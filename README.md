# E-Business Process Mining System

A web platform that turns raw e-commerce clickstream logs into process insights: discovered process models, graph metrics, a sales dashboard, next-step prediction and an LLM assistant that explains the results.

## Overview

Upload an event log (`view` → `cart` → `purchase`, grouped by user session) and the system:

1. discovers the shopping process with **process mining** algorithms,
2. analyses the session graph and e-commerce KPIs,
3. trains a **Transformer** model to predict a customer's next action,
4. lets you ask questions about the findings through a **Gemini**-powered chatbot.

Large files are handled with lazy/streaming loading (Polars) and session-level sampling, so multi-GB CSVs can be processed on a normal machine.

## Key features

- **Process discovery**: Alpha Miner and Inductive Miner, rendered as Petri nets and BPMN diagrams (pm4py + Graphviz).
- **Graph mining**: transition graph of user sessions with centrality metrics (NetworkX).
- **E-commerce dashboard**: funnel, conversion, category/brand and price charts (Matplotlib, Seaborn).
- **Next-activity prediction**: Transformer trained on session sequences (TensorFlow/Keras), exposed through `/api/predict`.
- **LLM insights and chat**: Gemini summarises findings and answers follow-up questions (optional, needs an API key).
- **Large-file friendly**: up to 6 GB uploads, with configurable caps on rows/sessions used for mining and training.

## Tech stack

| Layer | Technology |
|---|---|
| Web UI / gateway | Java 17, Spring Boot, Thymeleaf, JavaScript |
| Analytics service | Python 3.10/3.11, Flask, Polars, pandas, NumPy |
| Process mining | pm4py, NetworkX, Graphviz |
| Machine learning | TensorFlow / Keras, scikit-learn |
| LLM | Google Gemini (`google-genai`) |

## Architecture

```
Browser ──▶ web-app (Spring Boot :8080) ──▶ analytics-service (Flask :5000)
                │                                   │
                └──── shared folder: data/ ◀────────┘
```

The Spring Boot app serves the UI and forwards requests to the Flask service. Uploaded CSV files are written to `data/`, which both services share.

```
e-business-system/
├── analytics-service/
│   ├── app.py                    # Flask API: mining, dashboard, prediction, LLM
│   ├── app_with_evaluation.py    # Same API + /api/evaluation/download (model evaluation export)
│   └── requirements.txt
├── web-app/                      # Spring Boot application (Maven wrapper included)
│   └── src/main/{java,resources}
└── data/
    └── dataset.csv               # small sample event log
```

## Getting started

### Prerequisites

- Java 17+
- Python 3.10 or 3.11
- [Graphviz](https://graphviz.org/download/) installed on the OS and available on `PATH`
- About 8 GB of free RAM for large CSV files

### 1. Start the analytics service

```bash
cd analytics-service
python -m venv venv
source venv/bin/activate          # Windows: venv\Scripts\activate
pip install -r requirements.txt   # no GPU? replace tensorflow with tensorflow-cpu
python app.py
```

Open <http://localhost:5000>: you should see `API is running!`.

### 2. Start the web app

```bash
cd web-app
./mvnw spring-boot:run            # Windows: mvnw.cmd spring-boot:run
```

Open <http://localhost:8080>, upload a CSV (try `data/dataset.csv`) and wait for the dashboard.

### Configuration

| Setting | Where | Default | Purpose |
|---|---|---|---|
| `GEMINI_API_KEY` | env var (Python) | unset | Enables LLM insights and chat. Without it the rest of the app works. |
| `DATASET_DIR` | env var (Python) | `../data` | Folder that holds uploaded event logs |
| `python.api.base-url` | `web-app/src/main/resources/application.properties` | `http://localhost:5000` | Address of the analytics service |
| `dataset.dir` | `application.properties` | `../data` | Must point to the same folder as `DATASET_DIR` |
| `MAX_ROWS_FOR_PROCESS_MINING`, `MAX_SESSIONS_FOR_TRAINING` | top of `app.py` | 1.1M / 200k | Lower them for weaker machines |

```bash
export GEMINI_API_KEY="your-key"        # PowerShell: $env:GEMINI_API_KEY="your-key"
```

### Input format

CSV with at least these columns: `event_time, event_type, product_id, category_code, category_id, brand, price, user_id, user_session`.

## Troubleshooting

- **`Program dot not found`**: Graphviz is not installed or not on `PATH`.
- **`Connection refused` from the web app**: the Flask service is not running, or `python.api.base-url` is wrong.
- **Out of memory / slow on huge files**: reduce the caps listed above.
- **TensorFlow install fails**: use `tensorflow-cpu`.

## Notes

- `app.py` and `app_with_evaluation.py` share most of their code; the latter adds an evaluation export. Merging them into one module is a natural next refactor.
- The sample dataset is intentionally tiny; use a real e-commerce event log for meaningful models.
