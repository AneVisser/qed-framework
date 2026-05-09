# Ollama Setup

Ollama is a runtime for large language models (LLMs) designed to make running models locally easy and developer-friendly. It supports models like LLaMA, Mistral, Gemma, Phi, and others on macOS, Linux, and Windows.

Key characteristics:

- **API-first design** — when running, Ollama exposes a local HTTP API at `http://localhost:11434`
- **Optimised packaging** — models are bundled with weights, configs, and prompts in `.ollama` files
- **Prompt templates** — modelfiles (similar to Dockerfiles) can define system prompts, adapters, and fine-tuning

---

## Installing Ollama on Windows

1. Download the installer from [https://ollama.com/download](https://ollama.com/download)
2. Open a terminal and navigate to the download directory
3. Run the installer, optionally specifying a custom install directory:
   ```
   OllamaSetup.exe /DIR="D:\Tools\Ollama"
   ```
   If the directory parameter is omitted, Ollama installs to `C:\Users\<username>\.ollama`

---

## Moving Model Storage to a Different Drive

By default, models are stored in `C:\Users\<username>\.ollama\models`. To move them:

**Option 1 — Environment variable (preferred):**

1. Set the system environment variable `OLLAMA_MODELS` to your preferred directory
2. Ensure the directory exists and you have write permissions
3. Fully stop Ollama (service and any CLI instances)
4. Start Ollama again — new models will be downloaded to the new location

**Option 2 — Directory junction (fallback):**

If the environment variable approach does not take effect:

```
mklink /J "C:\Users\<username>\.ollama\models" "D:\Ollama\Models"
```

Then restart Ollama. It will transparently operate from the new location.

---

## Running Ollama on a Separate Machine

If Ollama is running on a different machine, run the following on that machine:

```
ollama serve
```

From the machine running the tests, verify connectivity:

```bash
curl http://192.168.56.1:11434/api/tags
```

---

## Installing Chroma

Chroma is used to store DSL patterns and page object embeddings. The following steps set up a local Chroma instance on Windows.

1. Create a directory for Chroma (e.g. `E:\Chroma`)
2. Create and activate a Python virtual environment:
   ```
   py -m venv E:\Chroma
   E:\Chroma\Scripts\activate.bat
   ```
3. Install Chroma:
   ```
   py -m pip install chromadb
   ```
4. Set the system environment variable:
   ```
   CHROMA_DB_DIR=E:\Chroma
   ```
5. Install FastAPI and Uvicorn:
   ```
   py -m pip install fastapi uvicorn
   ```
6. Create a wrapper subdirectory:
   ```
   mkdir E:\Chroma\wrapper
   ```
7. Copy `chromawrapper.py` from the QED repository at `/sut/kotlin/ollama/pythonwrapper/` into `E:\Chroma\wrapper`
8. Start the wrapper:
   ```
   uvicorn chroma_wrapper:app --host 127.0.0.1 --port 8501 --reload
   ```
9. Start the Chroma server:
   ```
   E:\Chroma\Scripts\chroma.exe run --port 8500
   ```
10. Install SentenceTransformers for embedding support:
    ```
    py -m pip install sentence-transformers
    ```

You can now run Chroma and Ollama scripts from QED.