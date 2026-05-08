# Design Principles

QED is built on a set of guiding principles that prioritise clarity, composability, and long-term maintainability. These principles inform every aspect of the framework — from DSL syntax to project structure, from test philosophy to architectural decisions.

---

## Clarity Over Cleverness

Tests should read like intent, not implementation. QED favours expressive, declarative syntax that makes test logic obvious at a glance. If a construct is clever but obscures meaning, it doesn't belong.

---

## Composability

Page objects, screen areas, actions, and assertions are designed to be modular and reusable. QED encourages composition over inheritance, allowing shared components to be reused across tests without duplication.

---

## Domain-Driven DSL

The DSL reflects what testers actually do:

- Navigate to the application
- Interact with pages
- Validate outcomes

This alignment with domain language makes tests easier to write, read, and maintain — even for those new to the framework.

---

## SOLID Architecture

QED adheres to SOLID principles to ensure the framework remains flexible and scalable:

- **Single Responsibility** — each component does one thing well
- **Open/Closed** — core abstractions are open for extension, closed for modification
- **Liskov Substitution** — interfaces and base types behave predictably
- **Interface Segregation** — DSLs and utilities expose only what is needed
- **Dependency Inversion** — high-level modules don't depend on low-level details

---

## Pragmatic Abstraction

QED avoids unnecessary layers. Abstractions are introduced only when they serve clarity, reuse, or separation of concerns. The goal is to reduce cognitive load — not to impress with complexity.

---

## Type Safety & Kotlin Idioms

Kotlin's type system is leveraged to catch errors early and guide correct usage. Null safety, extension functions, and DSL markers are used to make the framework intuitive and robust.

---

## Accessible Documentation

Documentation is treated as a first-class citizen. Every feature is documented with clean, markdown-first pages that prioritise onboarding and discoverability. The goal is to make QED feel premium without being opaque.

---

## Strategic Simplicity

QED is not trying to be everything to everyone. It is designed for teams who value modern automation practices, clean architecture, and expressive testing. Features are added selectively — only when they serve the core philosophy.