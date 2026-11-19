#!/usr/bin/env python3
"""Regenerate the Mermaid module graph in README.md and docs/architecture.md from the build files.

Usage: python3 scripts/module-graph.py          # rewrite the graphs between the marker comments
       python3 scripts/module-graph.py --check  # exit 1 if either file is out of date (run by ciCheck)

Nodes are the modules in settings.gradle.kts. Edges are main (non-test) `api`/`implementation` project
dependencies in each module's build.gradle.kts, in `projects.x.y` or `project(":x:y")` form, plus the project
dependencies the `unreel.android.feature` convention plugin adds to every feature module.
"""
import argparse
import glob
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
START, END = "<!-- module-graph:start -->", "<!-- module-graph:end -->"
FEATURE_CONVENTION = "build-logic/convention/src/main/kotlin/io/github/uveshm003/unreel/buildlogic/AndroidFeatureConventionPlugin.kt"
TARGETS = ["README.md", "docs/architecture.md"]
DEPENDENCY = re.compile(
    r'^\s*(?:api|implementation)\((?:projects\.([\w.]+)|project\("([^"]+)"\))\)', re.M
)


def read(rel):
    with open(os.path.join(ROOT, rel), encoding="utf-8") as handle:
        return handle.read()


def modules_from_settings():
    return set(re.findall(r'^include\("([^"]+)"\)', read("settings.gradle.kts"), re.M))


def accessor_to_path(accessor, modules):
    """`core.screenshotTesting` -> `:core:screenshot-testing`, validated against settings."""
    by_accessor = {
        re.sub(r"-(\w)", lambda m: m.group(1).upper(), path.strip(":").replace(":", ".")): path for path in modules
    }
    if accessor not in by_accessor:
        sys.exit(f"Unknown project accessor projects.{accessor} (not in settings.gradle.kts)")
    return by_accessor[accessor]


def feature_convention_deps(modules):
    deps = set(re.findall(r'add\("(?:api|implementation)",\s*project\("([^"]+)"\)\)', read(FEATURE_CONVENTION)))
    unknown = deps - modules
    if unknown:
        sys.exit(f"Feature convention depends on unknown modules: {sorted(unknown)}")
    return deps


def edges(modules):
    convention = feature_convention_deps(modules)
    result = set()
    for build_file in glob.glob(os.path.join(ROOT, "**", "build.gradle.kts"), recursive=True):
        rel = os.path.relpath(build_file, ROOT)
        parts = rel.split(os.sep)
        if parts[0] == "build-logic" or rel == "build.gradle.kts" or "build" in parts[:-1]:
            continue
        module = ":" + ":".join(parts[:-1])
        if module not in modules:
            sys.exit(f"{rel} is not included in settings.gradle.kts")
        text = read(rel)
        for accessor, path in DEPENDENCY.findall(text):
            dep = accessor_to_path(accessor, modules) if accessor else path
            if dep not in modules:
                sys.exit(f"{rel}: depends on unknown module {dep}")
            result.add((module, dep))
        if "unreel.android.feature" in text:
            result.update((module, dep) for dep in convention)
    return sorted(result)


def node_id(path):
    return path.strip(":").replace(":", "_").replace("-", "_")


def mermaid():
    modules = modules_from_settings()
    graph = edges(modules)
    groups = {}
    for m in sorted(modules):
        parts = m.strip(":").split(":")
        groups.setdefault(parts[0] if len(parts) > 1 else "", []).append(m)
    lines = ["```mermaid", "graph TD"]
    for group, members in sorted(groups.items()):
        indent = "        " if group else "    "
        if group:
            lines.append(f"    subgraph {group}")
        lines += [f"{indent}{node_id(m)}[{m.strip(':').split(':')[-1]}]" for m in members]
        if group:
            lines.append("    end")
    lines += [f"    {node_id(a)} --> {node_id(b)}" for a, b in graph]
    lines.append("```")
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--check", action="store_true", help="fail if a graph is out of date instead of writing")
    args = parser.parse_args()

    block = f"{START}\n{mermaid()}\n{END}"
    stale = []
    for rel in TARGETS:
        text = read(rel)
        if START not in text or END not in text:
            sys.exit(f"{rel}: missing {START} / {END} markers")
        updated = re.sub(re.escape(START) + r".*?" + re.escape(END), lambda _: block, text, flags=re.S)
        if updated != text:
            stale.append(rel)
            if not args.check:
                with open(os.path.join(ROOT, rel), "w", encoding="utf-8") as handle:
                    handle.write(updated)
    if args.check and stale:
        sys.exit("Module graph out of date in: " + ", ".join(stale) + ". Run: python3 scripts/module-graph.py")
    print("up to date" if not stale else "updated: " + ", ".join(stale))


if __name__ == "__main__":
    main()
