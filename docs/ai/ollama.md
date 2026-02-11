Ollama is a relatively new but growing project in the LLM space. 
It’s designed to make running large language models (LLMs) locally on your machine easy and developer-friendly. 
Here’s an overview and how you could use it as part of your own software:

- Runtime for LLMs: It lets you run models like LLaMA, Mistral, Gemma, Phi, etc., locally on macOS, Linux, and Windows (beta).
- Optimized packaging: Models are bundled in .ollama files with weights, configs, and prompts, so you don’t deal with raw HuggingFace weights manually.
- API-first design: When you run Ollama, it exposes a local HTTP API (http://localhost:11434) to interact with the models.
- Prompt templates: You can create “modelfiles” (like Dockerfiles) to define system prompts, adapters, and fine-tuning.

## Installation on local machine

- download installer from https://ollama.com/download
- open a CLI and log on to the directory where the installer has been downloaded.
- for Windows, you can run: **OllamaSetup.exe /DIR="D:\Tools\Ollama"** (or any other directory). If you omit the directory parameter, it will install to C:\Users\<username>\.ollama
- You may want to install the models to a separate drive/directory. To do so, you can take the following steps:
  - in system variables, set the variable **OLLAMA_MODELS** to the directory where you want the models to sit
  - Ensure the directory exists and that you have write permissions.
  - Completely stop Ollama (the service / app) and any CLI instances.
  - Start Ollama again (or run ollama from a fresh command prompt).
  - Test by pulling a new model or running a model and see where files get created.
- If the above doesn't work, you may create a WIndows redirect to the directory C:\Users\<username>\.ollama\models (where the models are installed by default):
  - in a CLI prompt, execute the following: **mklink /J "C:\Users\<username>\.ollama\models" "D:\Ollama\Models"** (or any other directory where you want the models to sit).
  - Restart Ollama **ollama run llama2**. It should transparently operate in the new location.
  - You can now run an Ollama kotlin script. 
  - when Ollama is running on a different machine, run 'Ollama serve' on that machine. Then on the (virtual) machine that runs the tests, access to ollama can be verified by running **'curl http://192.168.56.1:11434/api/tags  # Ollama'** 
  


## Installation of Chroma

In order to store DSL patterns, a copy of Chroma needs to be installed (there are other options available, but for a proof of concept, Chroma was chosen.)

- create a directory for Chroma
- create a virtual environment. run: **py -m venv E:\Chroma** (or any other directory where you want to install)
- run: **E:\Chroma\Scripts\activate.bat** (or activate.bat from your chosen directory)
- cd to your install directory (E:\Chroma in this example)
- run: **py -m pip install chromadb** (this install chroma db)
- run: **chromadb --path "E:/Chroma"**
- in WIndows environment (system) variables: set **CHROMA_DB_DIR=E:\Chroma**
- install fastcgi: run **py -m pip install fastapi uvicorn**
- create a subdirectory E:\Chroma\wrapper
- from the QED repository, copy the file /sut/kotlin/ollama/pythonwrapper/chromarapper.py to E:\Chroma\wrapper
- go to E:\chroma\wrapper, and run: **uvicorn chroma_wrapper:app --host 127.0.0.1 --port 8501 --reload**
- run: E:\Chroma\Scripts\chroma.exe run --port 8500 (that starts the chroma server on your port of preference)
- You can now run a chroma script in QED.
- For embedding, you will need SentenceTransormers from HuggingFace: **py -m pip install sentence-transformers**