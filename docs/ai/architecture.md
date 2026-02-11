## Chroma database

Chroma is a vector database, which stores semantic information. The idea is to store all 
information that is relevant for the system under test and which will be required to build 
test scripts from user stories.

The most important information here are the page objects. The ollama subdirectory contains a class PageIntrospection. 
This class contains methods using Kotlin reflection to scan all page objects of an application. For that purpose, all page
objects are defined in a **Page Registry**, from where they can be used by the application. 

As a rule of thumb, no test should use any page classes directly, but only the ones that are defined in the 
page registry.

A page object can be composed of different page area's, so that any combination of page area's only needs to be 
defined once. The PageIntrospection class creates data about the available functions and properties on a page, 
so that when creating the user stories, these can be used.

Each page area (especially the ones that have relevant properties and functions) should have a description annotation at the top, of this format:
```kotlin
annotation class PageMetadata(
    val description: String,
    val usage: String = "",
    val tags: Array<String> = [],
    val priority: Int = 0 // optional: for boosting relevance
)
```

At the top of each page, the annotation should be added as follows:

```kotlin
@PageMetadata("Relevant description of the magic happens on this page",
    usage = "onPage(magicPage) { }",
    tags =["MyApp", "Magic"]
)
```
The test ChromaTest.kt retrieves all relevant information of the pages, including the PageMetadata and available functions/properties, 
and stores them under the collection 'page-objects'.

Once the these data are stored, they can be retrieved using a query function.

The next step is to create the prompt that needs to be provided for Ollama. Therefore, we need an **embedding**.
This is just a vector (a list of numbers) that represents a piece of text in a way that captures its meaning.
Embeddings are the backbone of:
- Semantic search (finding text that “means the same thing” as a query).
- Retrieval-Augmented Generation (RAG) (retrieving relevant context for an LLM).
- Clustering / classification of text by meaning.

## Role of Ollama

Ollama is an LLM runtime that can run models locally (like LLaMA, Mistral, Gemma, etc.).
By itself, Ollama doesn’t store knowledge or run a vector database.
But Ollama can generate embeddings if you run an embedding-capable model.
For example: ollama run mxbai-embed-large can turn text into embeddings.
These embeddings can then be used as input to Chroma.

So: Ollama = where embeddings come from (or where LLM queries are processed).

## Role of Chroma

Chroma is a vector database.
Its job is to store embeddings and make them searchable.
It supports adding documents (with embeddings) and then performing similarity searches.
Example: You feed Chroma your knowledge base → query it with a user question → get the most relevant chunks → pass those back into Ollama to answer.

So: Chroma = where embeddings live and where you query them.

## Putting it together: Embeddings with Ollama + Chroma

Here’s the flow in a RAG pipeline:
- Embed your documents
  - Use Ollama with an embedding model → get vector representations of your docs.
  - Store them in Chroma.
- User asks a question
  - Embed the question with Ollama → get a vector for the query.
- Semantic search in Chroma
  - Chroma finds the most similar document chunks.
- Augment the LLM
  - Feed the retrieved chunks into Ollama with your query.
  - Ollama then answers using both the retrieved context + its own reasoning.

## In short:

- Ollama = brain (LLM + embeddings generator).
- Chroma = memory (stores and retrieves embeddings).
- Embeddings = language of meaning that lets the two work together.