# Origin & Motivation

QED wasn't born out of academic curiosity or a desire to reinvent the wheel — it was forged in the tension between legacy systems and the desire for clarity. It began with a simple, personal need: to test a web-based application I was building myself.

I wanted to follow a test-driven approach, with clean UI and API coverage. But as I began scaffolding the tests, all the familiar frustrations resurfaced — brittle selectors, opaque logic, and frameworks that made automation feel more like a burden than a craft. The tools I reached for professionally were no better. They actively resisted clarity, maintainability, and trust.

One framework from a reputable company stood out for all the wrong reasons. Its documentation contradicted itself. Its selectors were generated from element IDs so long they wouldn't fit on a widescreen. Its tooling produced 95% noise. Core functions silently failed or behaved unpredictably. It wasn't just inefficient — it was demoralizing.

Ironically, many test automation frameworks fail to meet the very standards they demand of the systems they test. They're brittle, opaque, and resistant to change — the opposite of what good engineering should be. That contradiction stayed with me.

---

## What Was Wrong

These frustrations aren't unique. They're endemic to the industry.

**Brittle selectors & slow tests** — Legacy UI frameworks often rely on fragile locator strategies: deeply nested XPath, unstable IDs, brittle CSS selectors. Tests break when the UI shifts even slightly, and debugging becomes a scavenger hunt.

**Opaque page objects** — Many frameworks treat page objects as monoliths: tightly coupled, hard to reuse, and full of duplicated logic. Shared components like headers or modals are redefined across tests, leading to bloat and inconsistency.

**Glue code & step definitions** — Tools like Cucumber promise readable tests but often deliver fragmented logic spread across step definitions, regex matchers, and external files. The result: unreadable suites and fragile test orchestration.

**Organizational inertia** — Even when better tools exist, change is hard. Teams get locked into legacy frameworks because it's what they've always used. Innovation stalls, and automation becomes a chore.

---

## The Spark

The spark came earlier in my career, when I was first introduced to Kotlin. Its elegance, type safety, and expressive syntax made it clear that automation could be something more — something readable, composable, and enjoyable to work with.

A colleague had developed a DSL-driven framework tailored to UI test automation for a specific application. While it was over-engineered in places, it revealed something powerful: that tests could speak the language of the domain. That automation could be declarative, not procedural. That clarity wasn't a luxury — it was a necessity.

Although the direction wasn't continued, the idea stayed with me.

---

## What QED Is

QED is a continuation of that idea — reimagined from first principles, built entirely from scratch, and shaped by the lessons of both success and resistance. It started as a solution for my own application, but quickly grew into something more: a framework that could serve the systems I test professionally, and others beyond.

It's designed to be:

- **Expressive**: Tests should read like intent, not implementation.
- **Composable**: Page objects, actions, and assertions should be modular and reusable.
- **SOLID**: Architecture should support change, not resist it.
- **Accessible**: Documentation should be clean, intuitive, and premium.

Some argue that we don't need more frameworks — just better practices. But frameworks and practices are inseparable. A framework either enables or constrains the way we work. Persisting with outdated or hostile tooling is, in itself, a bad practice. Good engineering deserves tools that support it.

QED isn't just a framework. It's a philosophy of how automation should feel — clear, confident, and built to last. Not perfect, not finished, but always evolving. And always built with respect for the engineers who use it.