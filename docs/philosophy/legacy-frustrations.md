QED wasn’t built in a vacuum. It was born out of years spent wrestling with brittle frameworks, slow feedback loops, and automation that felt more like a liability than a tool.

---

## Brittle Selectors & Slow Tests

Legacy UI frameworks often rely on fragile locator strategies — deeply nested XPath, unstable IDs, brittle CSS selectors. Tests break when the UI shifts even slightly, and debugging becomes a scavenger hunt.

QED uses modern locator strategies and encourages abstraction through composable screen areas, making tests resilient and maintainable.

---

## Opaque Page Objects

Many frameworks treat page objects as monoliths — tightly coupled, hard to reuse, and full of duplicated logic. Shared components like headers or modals are redefined across tests, leading to bloat and inconsistency.

QED embraces composition. Page objects can be built from reusable screen areas, reducing duplication and improving clarity.

---

## Glue Code & Step Definitions

Tools like Cucumber promise readable tests, but often deliver fragmented logic spread across step definitions, regex matchers, and external files. The result: unreadable suites and fragile test orchestration.

QED supports Given–When–Then semantics without the baggage. Behavior-driven syntax is expressive, not prescriptive — no glue code required.

---

## Legacy Language Constraints

Some frameworks are stuck in outdated languages or paradigms that resist modern practices. Kotlin, despite its elegance and safety, is sometimes dismissed as niche — leading to missed opportunities for cleaner, more expressive automation.

QED embraces Kotlin fully, leveraging its type system, DSL capabilities, and modern concurrency tools to build tests that are both robust and readable.

---

## Organizational Inertia

Even when better tools exist, change is hard. Teams get locked into legacy frameworks because “it’s what we’ve always used.” Innovation stalls, and automation becomes a chore.

QED is designed to be easy to adopt, easy to extend, and easy to love. It’s a framework for teams who are ready to move forward — not just maintain the status quo.

---

## A Framework That Respects Your Time

QED is a response to these frustrations. It’s not trying to be everything — just the right thing. It’s built for clarity, composability, and speed. For engineers who want automation to feel like a craft again.

