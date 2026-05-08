# Chroma Database

Chroma is a vector database that stores semantic information. The idea is to store all information relevant to the system under test that will be required to build test scripts from user stories — most importantly, the page objects.

---

## Page Introspection

The `ollama` subdirectory contains a class `PageIntrospection`. This class uses Kotlin reflection to scan all page objects of an application. For this purpose, all page objects are defined in a **Page Registry**, from where they can be used by the application.

As a rule of thumb, no test should use page classes directly — only those defined in the page registry.

A page object can be composed of different page areas, so that any combination only needs to be defined once. `PageIntrospection` creates data about the available functions and properties on each page so that when generating tests from user stories, these can be referenced.

---

## Page Metadata Annotations

Each page area — especially those with relevant properties and functions — should have a description annotation:

```kotlin
annotation class PageMetadata(
    val description: String,
    val usage: String = "",
    val tags: Array<String> = [],
    val priority: Int = 0 // optional: for boosting relevance
)
```

Add the annotation at the top of each page class:

```kotlin
@PageMetadata(
    "Relevant description of what happens on this page",
    usage = "onPage(magicPage) { }",
    tags = ["MyApp", "Magic"]
)
```

The test `ChromaTest.kt` retrieves all relevant information from the pages — including `PageMetadata` and available functions and properties — and stores them in Chroma under the collection `page-objects`. Once stored, they can be retrieved using a query function.

---

## Embeddings

To create the prompt for Ollama, an **embedding** is needed. An embedding is a vector — a list of numbers — that represents a piece of text in a way that captures its meaning. Embeddings are the backbone of:

- **Semantic search** — finding text that means the same thing as a query
- **Retrieval-Augmented Generation (RAG)** — retrieving relevant context for an LLM
- **Clustering and classification** — grouping text by meaning

---

## Role of Ollama

Ollama is an LLM runtime that runs models locally (LLaMA, Mistral, Gemma, etc.). It does not store knowledge or run a vector database, but it can generate embeddings when running an embedding-capable model — for example, `mxbai-embed-large`. Those embeddings are then used as input to Chroma.

**Ollama = where embeddings come from, and where LLM queries are processed.**

---

## Role of Chroma

Chroma is a vector database. Its job is to store embeddings and make them searchable — adding documents with embeddings and performing similarity searches against them.

**Chroma = where embeddings live and where you query them.**

---

## The RAG Pipeline

The full flow for retrieval-augmented generation in QED:

1. **Embed your documents** — use Ollama with an embedding model to get vector representations of your page objects, then store them in Chroma
2. **User provides a user story** — embed the query with Ollama to get a vector representation
3. **Semantic search in Chroma** — Chroma finds the most similar page object chunks
4. **Augment the LLM** — feed the retrieved chunks into Ollama alongside the query; Ollama answers using both the retrieved context and its own reasoning

In summary:
- **Ollama** — brain (LLM + embeddings generator)
- **Chroma** — memory (stores and retrieves embeddings)
- **Embeddings** — the language of meaning that lets the two work together