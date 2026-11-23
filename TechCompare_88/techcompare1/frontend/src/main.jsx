import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  ArrowUpRight,
  Check,
  ChevronUp,
  Laptop,
  Moon,
  Plus,
  Search,
  Smartphone,
  Sun,
  X,
} from "lucide-react";
import "./style.css";

const API = "/api/v1";
const BACKEND_ORIGIN = window.location.origin;

const CATEGORIES = ["GAMING", "CREATIVE_WORK", "DAILY_USE"];
const CATEGORY_NAMES = {
  GAMING: "Gaming",
  CREATIVE_WORK: "Creative work",
  DAILY_USE: "Daily use",
};

// Все запросы к Spring Boot проходят через одну функцию.
// Так ошибки API одинаково обрабатываются для каталога, сравнения и AI.
async function request(path, options = {}) {
  const response = await fetch(API + path, {
    ...options,
    headers: {
      ...(options.body ? { "Content-Type": "application/json" } : {}),
      ...(options.headers || {}),
    },
  });

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json")
    ? await response.json().catch(() => null)
    : await response.text().catch(() => "");

  if (!response.ok) {
    const detail =
      typeof payload === "string"
        ? payload
        : payload?.detail || payload?.message || payload?.error || payload?.title || "";

    throw new Error(`API ${response.status}${detail ? `: ${detail}` : ""}`);
  }

  return payload;
}

// Backend может вернуть абсолютный URL или относительный путь к картинке.
// Относительный путь направляем к Spring Boot, а не к Vite.
function getImageUrl(url) {
  if (!url) return "";
  if (/^(https?:|data:|blob:)/i.test(url)) return url;
  return `${BACKEND_ORIGIN}${url.startsWith("/") ? "" : "/"}${url}`;
}

function value(device, key) {
  const val = device[key];
  if (val === true) return "Yes";
  if (val === false) return "No";
  return val ?? "—";
}

function DeviceImage({ device, type, className = "" }) {
  const [broken, setBroken] = useState(false);
  const imageUrl = getImageUrl(device.imageUrl);

  if (imageUrl && !broken) {
    return (
      <img
        className={`device-image ${className}`}
        src={imageUrl}
        alt={device.name}
        onError={() => setBroken(true)}
      />
    );
  }

  return (
    <div className={`image-placeholder ${className}`} aria-label="No device image">
      {type === "smartphones" ? <Smartphone size={38} /> : <Laptop size={38} />}
    </div>
  );
}

function DeviceSuggestion({ device, type, selected, onSelect }) {
  const isSmartphone = type === "smartphones";

  const extraSpecs = isSmartphone
    ? [
        device.ramGb != null ? `${device.ramGb} GB RAM` : null,
        device.storageGb != null ? `${device.storageGb} GB storage` : null,
        device.mainCameraMp != null ? `${device.mainCameraMp} MP camera` : null,
      ]
    : [
        device.ramGb != null ? `${device.ramGb} GB RAM` : null,
        device.storageGb != null ? `${device.storageGb} GB storage` : null,
        device.screenSizeInch != null ? `${device.screenSizeInch}" display` : null,
      ];

  return (
    <button
      type="button"
      className={`suggestion-card ${selected ? "selected" : ""}`}
      onClick={() => onSelect(device.id)}
      role="option"
      aria-selected={selected}
    >
      <div className="suggestion-image">
        <DeviceImage device={device} type={type} />
      </div>

      <div className="suggestion-content">
        <div className="suggestion-topline">
          <span className="suggestion-brand">{device.brand || "Device"}</span>
          {selected && (
            <span className="selected-badge">
              <Check size={12} /> Selected
            </span>
          )}
        </div>
        <strong>{device.name}</strong>
        <div className="suggestion-specs">
          {extraSpecs.filter(Boolean).map((spec) => (
            <span key={spec}>{spec}</span>
          ))}
        </div>
      </div>

      <span className={`suggestion-action ${selected ? "selected" : ""}`}>
        {selected ? <Check size={17} /> : <Plus size={17} />}
        <span>{selected ? "Selected" : "Add"}</span>
      </span>
    </button>
  );
}

function SpiderChart({ results, category, categoryWeights = {} }) {
  /*
   * Radar chart has two deliberately different modes.
   *
   * OVERALL:
   *   The backend returns exactly three category scores, so the chart is a
   *   triangle: Gaming, Creative work and Daily use.
   *
   * A single category:
   *   We use the metric details returned by the backend. Most category
   *   profiles contain five metrics, while Daily Use contains six. To keep
   *   the single-category chart visually stable as a hexagon, a five-metric
   *   profile gets one additional, explicitly labelled "Category score"
   *   axis. That value is the exact category score from the backend, not a
   *   frontend calculation. A six-metric profile uses all six backend
   *   metrics directly.
   */
  const size = 420;
  const center = size / 2;
  const radius = 132;
  const isOverall = category === "OVERALL";

  const getMetricAxes = () => {
    if (isOverall) {
      return CATEGORIES.map((categoryKey) => ({
        key: categoryKey,
        name: CATEGORY_NAMES[categoryKey],
        kind: "category",
      }));
    }

    const firstCategory = results.find((item) => item.categories?.[category]);
    const metrics = firstCategory?.categories?.[category]?.metrics || [];

    const axes = metrics.map((metric) => ({
      key: metric.id,
      name: metric.name,
      kind: "metric",
    }));

    // Gaming and Creative Work currently expose five backend metrics.
    // Add the backend category total as the sixth reference axis so the
    // requested single-category chart always remains a clean hexagon.
    if (axes.length === 5) {
      axes.push({
        key: "CATEGORY_SCORE",
        name: "Category score",
        kind: "category-score",
      });
    }

    return axes.slice(0, 6);
  };

  const axes = getMetricAxes();

  const getScore = (item, axis) => {
    if (axis.kind === "category") {
      const rawScore = Number(item.categoryScores?.[axis.key] ?? 0);
      const weight = Number(categoryWeights?.[axis.key] ?? 0);

      // Backend categoryScores can already contain the selected weight.
      // Normalize the contribution for the radar so a 34/33/33 split does
      // not collapse the whole triangle toward the centre. The weight still
      // changes the radar shape, but the 0-100 visual scale stays stable.
      if (weight > 0 && weight <= 100 && rawScore <= weight + 0.5) {
        const maxWeight = Math.max(
          1,
          ...CATEGORIES.map((key) => Number(categoryWeights?.[key] ?? 0))
        );
        return Math.min(100, (rawScore * 100) / maxWeight);
      }

      return rawScore;
    }

    if (axis.kind === "category-score") {
      return item.categories?.[category]?.score ?? item.categoryScores?.[category];
    }

    const metric = item.categories?.[category]?.metrics?.find(
      (entry) => entry.id === axis.key
    );
    return metric?.score;
  };

  const angle = (index) =>
    -Math.PI / 2 + (index * Math.PI * 2) / axes.length;

  const point = (index, score, labelRadius = radius) => {
    // Null means the backend says that the metric is unavailable.
    // It is drawn at the centre instead of being silently invented.
    const safeScore = Math.max(0, Math.min(100, Number(score) || 0));
    const currentAngle = angle(index);
    const currentRadius = (labelRadius * safeScore) / 100;

    return {
      x: center + Math.cos(currentAngle) * currentRadius,
      y: center + Math.sin(currentAngle) * currentRadius,
    };
  };

  const labelPoint = (index) => {
    const currentAngle = angle(index);
    const labelRadius = radius + 26;
    return {
      x: center + Math.cos(currentAngle) * labelRadius,
      y: center + Math.sin(currentAngle) * labelRadius,
    };
  };

  const gridPoints = (factor) =>
    axes
      .map((_, index) => {
        const currentAngle = angle(index);
        const currentRadius = radius * factor;
        return `${center + Math.cos(currentAngle) * currentRadius},${
          center + Math.sin(currentAngle) * currentRadius
        }`;
      })
      .join(" ");

  if (!axes.length) {
    return (
      <div className="spider-empty">
        Backend did not return metric details for this category.
      </div>
    );
  }

  return (
    <div className="spider-chart-wrap">
      <div className="spider-chart-box">
        <svg
          className="spider-chart"
          viewBox={`0 0 ${size} ${size}`}
          role="img"
          aria-label={`${isOverall ? "Category" : "Metric"} comparison radar chart`}
        >
          {/* Concentric reference polygons keep every chart on the same 0–100 scale. */}
          {[0.2, 0.4, 0.6, 0.8, 1].map((level) => (
            <polygon
              key={level}
              className="spider-grid"
              points={gridPoints(level)}
            />
          ))}

          {/* One axis per backend category or metric. */}
          {axes.map((axis, index) => {
            const outer = point(index, 100);
            return (
              <line
                key={axis.key}
                className="spider-axis"
                x1={center}
                y1={center}
                x2={outer.x}
                y2={outer.y}
              />
            );
          })}

          {/* One filled polygon per compared device. */}
          {results.map((item, index) => {
            const points = axes
              .map((axis, axisIndex) => {
                const currentPoint = point(axisIndex, getScore(item, axis));
                return `${currentPoint.x},${currentPoint.y}`;
              })
              .join(" ");

            return (
              <polygon
                key={item.deviceId}
                className={`spider-data spider-data-${index % 4}`}
                points={points}
              />
            );
          })}

          {/* Exact score points make the individual values easy to read. */}
          {results.flatMap((item, itemIndex) =>
            axes.map((axis, axisIndex) => {
              const currentPoint = point(axisIndex, getScore(item, axis));

              return (
                <circle
                  key={`${item.deviceId}-${axis.key}`}
                  className={`spider-point spider-point-${itemIndex % 4}`}
                  cx={currentPoint.x}
                  cy={currentPoint.y}
                  r="4"
                />
              );
            })
          )}

          {/* Labels are placed outside the polygon so they never overlap the data. */}
          {axes.map((axis, index) => {
            const label = labelPoint(index);
            const anchor =
              label.x < center - 10
                ? "end"
                : label.x > center + 10
                  ? "start"
                  : "middle";

            return (
              <text
                key={axis.key}
                className="spider-label"
                x={label.x}
                y={label.y}
                textAnchor={anchor}
                dominantBaseline="middle"
              >
                {axis.name}
              </text>
            );
          })}
        </svg>
      </div>

      <div className="spider-legend">
        <div className="spider-mode-label">
          {isOverall
            ? "3 backend categories"
            : `${axes.length} backend comparison axes`}
        </div>

        {results.map((item, index) => (
          <div className="spider-legend-row" key={item.deviceId}>
            <span className={`spider-legend-dot spider-dot-${index % 4}`} />
            <span>{item.deviceName}</span>
            <b>
              {isOverall
                ? `${Number(item.overallScore ?? 0).toFixed(1)} / 100`
                : `${Number(item.categoryScores?.[category] ?? 0).toFixed(0)} / 100`}
            </b>
          </div>
        ))}
      </div>
    </div>
  );
}

function ScoreChart({ results, category }) {
  const max = Math.max(
    100,
    ...results.map((item) =>
      category === "OVERALL"
        ? Number(item.overallScore || 0)
        : Number(item.categoryScores?.[category] || 0)
    )
  );

  return (
    <div className="chart" role="img" aria-label="Device score comparison">
      {results.map((item, index) => {
        const score =
          category === "OVERALL"
            ? Number(item.overallScore || 0)
            : Number(item.categoryScores?.[category] || 0);

        return (
          <div className="chart-row" key={item.deviceId}>
            <div className="chart-label">
              <span>{item.deviceName}</span>
              <b>{score}</b>
            </div>
            <div className="bar-track">
              <div
                className={`bar bar-${index % 4}`}
                style={{ width: `${Math.max(0, Math.min(100, (score / max) * 100))}%` }}
              />
            </div>
          </div>
        );
      })}
      <div className="chart-scale">
        <span>0</span>
        <span>{max} points</span>
      </div>
    </div>
  );
}

// Изменение одного веса автоматически перераспределяет оставшийся процент
// между двумя другими категориями. Поэтому сумма всегда остаётся ровно 100%.
// Это лучше, чем позволять пользователю собрать, например, 137% при помощи
// трёх ползунков, потому что математика тоже иногда хочет жить.
function updateCategoryWeight(current, key, rawValue) {
  const nextValue = Math.max(0, Math.min(100, Math.round(Number(rawValue) || 0)));
  const otherKeys = CATEGORIES.filter((category) => category !== key);
  const oldOtherTotal = otherKeys.reduce(
    (sum, category) => sum + Math.max(0, Number(current[category]) || 0),
    0
  );
  const remaining = 100 - nextValue;
  const next = { ...current, [key]: nextValue };

  // Если у двух остальных категорий раньше были нулевые веса,
  // делим оставшееся место поровну.
  if (oldOtherTotal === 0) {
    const first = Math.floor(remaining / 2);
    next[otherKeys[0]] = first;
    next[otherKeys[1]] = remaining - first;
    return next;
  }

  // В остальных случаях сохраняем их прежнее соотношение.
  const firstOther = Math.round(
    (Number(current[otherKeys[0]]) / oldOtherTotal) * remaining
  );
  next[otherKeys[0]] = Math.max(0, Math.min(remaining, firstOther));
  next[otherKeys[1]] = remaining - next[otherKeys[0]];

  return next;
}

function App() {
  const [type, setType] = useState("smartphones");
  const [devices, setDevices] = useState([]);
  const [selected, setSelected] = useState([]);
  const [query, setQuery] = useState("");
  const [dark, setDark] = useState(
    () => localStorage.getItem("tc-theme") === "dark"
  );
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [comparison, setComparison] = useState(null);
  const [activeCategory, setActiveCategory] = useState("OVERALL");
  // Вес каждой категории хранится сразу в процентах.
  // Начальные значения уже дают ровно 100%.
  const [weights, setWeights] = useState({
    GAMING: 34,
    CREATIVE_WORK: 33,
    DAILY_USE: 33,
  });
  const [aiText, setAiText] = useState("");
  const [comparing, setComparing] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);

  // При смене типа устройства загружаем только нужный endpoint Backend.
  useEffect(() => {
    setLoading(true);
    setError("");
    setSelected([]);
    setComparison(null);
    setAiText("");
    setQuery("");

    request(`/${type}`)
      .then((data) => setDevices(Array.isArray(data) ? data : []))
      .catch((requestError) => {
        setError(
          `Could not load the catalogue: ${requestError?.message || "Backend is unavailable."}`
        );
      })
      .finally(() => setLoading(false));
  }, [type]);

  useEffect(() => {
    localStorage.setItem("tc-theme", dark ? "dark" : "light");
    document.documentElement.dataset.theme = dark ? "dark" : "light";
  }, [dark]);

  // Поиск выполняется прямо во Frontend по уже загруженному каталогу Backend.
  // Пока пользователь ничего не ввёл, полный список специально не показываем.
  // Поиск сортирует совпадения по качеству: сначала начало названия/бренда,
  // затем позиция первого совпадения. Поэтому при вводе одной буквы
  // устройства с этой буквой в начале поднимаются наверх.
  // Специальная команда "All" показывает весь каталог без ограничения в 8 строк.
  const isShowAll = query.trim().toLocaleLowerCase() === "all";

  const filtered = useMemo(() => {
    const term = query.trim().toLocaleLowerCase();
    if (!term) return [];
    if (term === "all") return devices;

    return devices
      .map((device, originalIndex) => {
        const name = (device.name || "").toLocaleLowerCase();
        const brand = (device.brand || "").toLocaleLowerCase();
        const full = `${name} ${brand}`;
        const nameIndex = name.indexOf(term);
        const brandIndex = brand.indexOf(term);
        const fullIndex = full.indexOf(term);

        // Чем меньше score, тем выше устройство в результатах.
        // Начало названия важнее совпадения где-то в середине.
        const startsInName = name.startsWith(term);
        const startsInBrand = brand.startsWith(term);
        const wordStartsInName = name.split(/\s+/).some((word) => word.startsWith(term));

        let priority = 3;
        if (startsInName || startsInBrand) priority = 0;
        else if (wordStartsInName) priority = 1;
        else if (fullIndex >= 0) priority = 2;

        return {
          device,
          priority,
          position: Math.min(
            nameIndex >= 0 ? nameIndex : Infinity,
            brandIndex >= 0 ? brandIndex : Infinity,
            fullIndex >= 0 ? fullIndex : Infinity
          ),
          originalIndex,
        };
      })
      .filter((item) => item.position !== Infinity)
      .sort((a, b) =>
        a.priority - b.priority ||
        a.position - b.position ||
        String(a.device.name || "").localeCompare(String(b.device.name || "")) ||
        a.originalIndex - b.originalIndex
      )
      .map((item) => item.device);
  }, [devices, query]);

  // Кнопка "Наверх" появляется только после небольшой прокрутки.
  const [showScrollTop, setShowScrollTop] = useState(false);
  const [confetti, setConfetti] = useState([]);

  useEffect(() => {
    const handleScroll = () => setShowScrollTop(window.scrollY > 500);
    window.addEventListener("scroll", handleScroll, { passive: true });
    handleScroll();
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const selectedDevices = devices.filter((device) =>
    selected.includes(Number(device.id))
  );

  function toggleDevice(rawId) {
    const id = Number(rawId);

    if (!Number.isInteger(id)) {
      setError("This device has an invalid ID. Reload the catalogue and try again.");
      return;
    }

    setSelected((current) => {
      if (current.includes(id)) {
        return current.filter((item) => item !== id);
      }

      if (current.length >= 4) {
        setError("You can compare up to 4 devices at once.");
        return current;
      }

      return [...current, id];
    });

    setComparison(null);
    setAiText("");
    setError("");
  }

  function selectDevice(id) {
    // Keep the current query visible after selection.
    // This lets the user add several matching devices without searching again.
    toggleDevice(id);
  }

  async function fetchAiOverview(comparisonData) {
    if (!comparisonData) return;

    setAiLoading(true);
    setAiText("");

    try {
      const result = await request("/ai/overview", {
        method: "POST",
        body: JSON.stringify({
          deviceType: type === "laptops" ? "laptop" : "smartphone",
          categoryWeights: comparisonData.categoryWeights,
          results: (comparisonData.results || []).map((item) => ({
            deviceId: Number(item.deviceId),
            deviceName: item.deviceName,
            brand: item.brand,
            overallScore: item.overallScore,
            availableCategoryWeight: item.availableCategoryWeight,
            rank: item.rank,
            categoryScores: item.categoryScores,
          })),
        }),
      });

      setAiText(
        result.overview || result.text || "AI did not return an explanation."
      );
    } catch (requestError) {
      setAiText(
        `AI summary could not be generated: ${requestError?.message || "Check GEMINI_API_KEY and the backend logs."}`
      );
    } finally {
      setAiLoading(false);
    }
  }

  function launchConfetti() {
    const pieces = Array.from({ length: 70 }, (_, index) => ({
      id: `${Date.now()}-${index}`,
      left: 50 + (Math.random() - 0.5) * 16,
      delay: Math.random() * 0.12,
      duration: 1.7 + Math.random() * 1.1,
      drift: (Math.random() - 0.5) * 520,
      rotate: Math.random() * 720 - 360,
      color: ["#60a5fa", "#3b82f6", "#93c5fd", "#ffffff", "#22c55e"][index % 5],
    }));

    setConfetti(pieces);
    window.setTimeout(() => setConfetti([]), 3200);
  }

  async function compare() {
    if (selected.length < 2) return;

    setComparing(true);
    setError("");
    setAiText("");

    try {
      // Backend принимает целые проценты, сумма которых должна быть ровно 100.
      const raw = CATEGORIES.map((category) =>
        Math.max(0, Number(weights[category]) || 0)
      );
      const total = raw.reduce((sum, item) => sum + item, 0);

      if (total <= 0) {
        throw new Error("Choose at least one comparison priority.");
      }

      const normalized = raw.map((value) => Math.floor((value * 100) / total));
      let remainder = 100 - normalized.reduce((sum, item) => sum + item, 0);

      for (let index = 0; remainder > 0; index = (index + 1) % normalized.length) {
        if (raw[index] > 0) {
          normalized[index] += 1;
          remainder -= 1;
        }
      }

      const categoryWeights = Object.fromEntries(
        CATEGORIES.map((category, index) => [category, normalized[index]])
      );
      const deviceIds = selected.map(Number);

      if (
        deviceIds.some((id) => !Number.isInteger(id)) ||
        new Set(deviceIds).size !== deviceIds.length
      ) {
        throw new Error("The selected devices have invalid or duplicate IDs.");
      }

      const result = await request(`/comparisons/${type}`, {
        method: "POST",
        body: JSON.stringify({ deviceIds, categoryWeights }),
      });

      if (!result || !Array.isArray(result.results) || result.results.length < 2) {
        throw new Error(
          "The backend returned no comparison results. Check that the selected devices exist in the database."
        );
      }

      setComparison(result);
      setActiveCategory("OVERALL");
      await fetchAiOverview(result);
    } catch (requestError) {
      setError(
        `Comparison failed: ${requestError?.message || "Check backend logs and selected devices."}`
      );
    } finally {
      setComparing(false);
    }
  }

  async function explainComparison() {
    if (!comparison || aiLoading) return;
    await fetchAiOverview(comparison);
  }

  const results = comparison?.results || [];

  // Защита интерфейса: этот показатель всегда должен быть 100.
  const weightTotal = CATEGORIES.reduce(
    (sum, category) => sum + Number(weights[category] || 0),
    0
  );

  return (
    <div className="app-shell">
      <header className="topbar">
        <a
          className="brand"
          href="/"
          onClick={(event) => {
            event.preventDefault();
            window.location.reload();
          }}
          aria-label="Refresh TechCompare"
        >
          <span className="brand-mark">T</span>
          Tech<span>Compare</span>
        </a>

        <nav className="nav-tabs" aria-label="Device type">
          <button
            className={type === "smartphones" ? "nav-tab active" : "nav-tab"}
            onClick={() => setType("smartphones")}
          >
            <Smartphone size={17} />
            Smartphones
          </button>
          <button
            className={type === "laptops" ? "nav-tab active" : "nav-tab"}
            onClick={() => setType("laptops")}
          >
            <Laptop size={17} />
            Laptops
          </button>
        </nav>

        <button
          className="theme-toggle"
          onClick={() => setDark((current) => !current)}
          aria-label="Toggle dark theme"
        >
          {dark ? <Sun size={19} /> : <Moon size={19} />}
        </button>
      </header>

      <main className="main-content">
        <section className="hero">
          <div className="eyebrow">
            <span className="dot" /> SMARTER TECH DECISIONS
          </div>
          <h1>
            Find your next <span>favorite device.</span>
          </h1>
          <p>Compare real specs. Understand the scores. Choose with confidence.</p>
        </section>

        {error && <div className="error-box">{error}</div>}

        <section className="catalog-section">
          <div className="section-heading">
            <div>
              <div className="eyebrow">THE CATALOGUE</div>
              <h2>Choose devices to compare</h2>
              <p className="section-subtitle">
                Search by brand or model. Best matches appear first; type “All” to show the full catalogue.
              </p>
            </div>

            <div className="search-autocomplete">
              <label className="search-box">
                <Search size={18} />
                <input
                  value={query}
                  onChange={(event) => setQuery(event.target.value)}
                  placeholder="Search Apple, Samsung, MacBook…"
                  autoComplete="off"
                  aria-label="Search devices"
                />
                {query && (
                  <button
                    type="button"
                    className="clear-search"
                    onClick={() => setQuery("")}
                    aria-label="Clear search"
                  >
                    <X size={16} />
                  </button>
                )}
              </label>

              {query.trim() && !loading && (
                <div className="suggestions" role="listbox" aria-label="Matching devices">
                  {filtered.length === 0 ? (
                    <div className="suggestion-empty">
                      No devices found for “{query}”.
                    </div>
                  ) : (
                    (isShowAll ? filtered : filtered.slice(0, 8)).map((device) => (
                      <DeviceSuggestion
                        key={device.id}
                        device={device}
                        type={type}
                        selected={selected.includes(Number(device.id))}
                        onSelect={selectDevice}
                      />
                    ))
                  )}

                  {filtered.length > 8 && !isShowAll && (
                    <div className="suggestion-foot">
                      Showing the 8 best matches. Type more letters to narrow the list, or type “All” to show the full catalogue.
                    </div>
                  )}

                  {isShowAll && filtered.length > 0 && (
                    <div className="suggestion-foot all-results-foot">
                      Showing all {filtered.length} devices.
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {loading ? (
            <div className="loading">Loading catalogue…</div>
          ) : !query.trim() ? (
            <div className="empty search-prompt">
              <Search size={30} />
              <strong>Start typing to find a device</strong>
              <span>
                The full catalogue stays hidden until you search, so you only see relevant devices.
              </span>
            </div>
          ) : filtered.length === 0 ? (
            <div className="empty">No devices found. Try another brand or model.</div>
          ) : (
            <div className="search-result-note">
              {filtered.length} matching {filtered.length === 1 ? "device" : "devices"}.
              Select the devices you want to compare above.
            </div>
          )}

          {selected.length > 0 && (
            <div className="selected-panel">
              <div className="selected-panel-head">
                <div>
                  <strong>Selected devices</strong>
                  <span>Choose up to 4 devices</span>
                </div>
                <b>{selected.length}/4</b>
              </div>

              <div className="selected-device-list">
                {selectedDevices.map((device) => (
                  <div className="selected-device" key={device.id}>
                    <DeviceImage
                      device={device}
                      type={type}
                      className="selected-device-image"
                    />
                    <div className="selected-device-info">
                      <small>{device.brand || "Device"}</small>
                      <strong>{device.name}</strong>
                    </div>
                    <button
                      type="button"
                      onClick={() => toggleDevice(device.id)}
                      aria-label={`Remove ${device.name}`}
                    >
                      <X size={16} />
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="compare-bar">
            <div>
              <b>
                {selected.length} of 4 devices selected
              </b>
              <span>
                {selected.length < 2
                  ? "Select at least 2 devices to compare."
                  : "Ready for an exact backend comparison."}
              </span>
            </div>
            <button
              className="primary-button"
              onClick={compare}
              disabled={selected.length < 2 || comparing}
            >
              {comparing ? "Comparing…" : "Compare devices"}
              <ArrowUpRight size={17} />
            </button>
          </div>
        </section>

        {selectedDevices.length > 0 && (
          <section className="spec-section">
            <div className="section-heading">
              <div>
                <div className="eyebrow">YOUR SHORTLIST</div>
                <h2>Selected specifications</h2>
              </div>
            </div>

            <div className="spec-grid">
              {selectedDevices.map((device) => (
                <div className="spec-card" key={device.id}>
                  <div className="spec-card-head">
                    <div className="spec-device-title">
                      <DeviceImage
                        device={device}
                        type={type}
                        className="spec-card-image"
                      />
                      <div>
                        <small>{device.brand || "Device"}</small>
                        <strong>{device.name}</strong>
                      </div>
                    </div>
                    <button
                      type="button"
                      onClick={() => toggleDevice(device.id)}
                      aria-label={`Remove ${device.name}`}
                    >
                      <X size={16} />
                    </button>
                  </div>

                  {(type === "smartphones"
                    ? [
                        ["Geekbench multi-core", "geekbenchMulti"],
                        ["Geekbench single-core", "geekbenchSingle"],
                        ["GPU benchmark", "antutuGpuScore"],
                        ["Main camera", "mainCameraMp", "MP"],
                        ["Front camera", "frontCameraMp", "MP"],
                        ["Refresh rate", "screenRefreshHz", "Hz"],
                        ["Brightness", "screenBrightnessNits", "nits"],
                        ["Weight", "weightG", "g"],
                        ["5G", "has5g"],
                      ]
                    : [
                        ["Geekbench multi-core", "geekbenchMulti"],
                        ["Geekbench single-core", "geekbenchSingle"],
                        ["GPU benchmark", "gpuBenchmarkScore"],
                        ["Screen resolution", "screenResolutionPixels", "px"],
                        ["Screen size", "screenSizeInch", "in"],
                        ["Panel", "panelType"],
                        ["Touchscreen", "hasTouchscreen"],
                        ["Weight", "weightKg", "kg"],
                        ["Battery life", "batteryLifeHours", "hours"],
                      ]
                  ).map(([label, key, unit]) => (
                    <div className="spec-row" key={key}>
                      <span>{label}</span>
                      <b>
                        {value(device, key)}
                        {unit && device[key] != null ? ` ${unit}` : ""}
                      </b>
                    </div>
                  ))}
                </div>
              ))}
            </div>
          </section>
        )}

        {comparison && (
          <section className="results-section">
            <div className="section-heading">
              <div>
                <div className="eyebrow">THE RESULTS</div>
                <h2>Comparison overview</h2>
                <p className="muted">
                  Scores are calculated by the backend from the available device metrics.
                </p>
              </div>
              <button
                className="secondary-button"
                onClick={explainComparison}
                disabled={aiLoading}
              >
                {aiLoading ? "Generating AI overview…" : "Explain results with AI"}
              </button>
            </div>

            <div className="results-layout">
              <div className="panel main-chart-panel">
                <div className="panel-heading">
                  <div>
                    <h3>
                      {activeCategory === "OVERALL"
                        ? "Overall score"
                        : CATEGORY_NAMES[activeCategory]}
                    </h3>
                    <p>Higher score means a better match for this use case.</p>
                  </div>
                  <span className="score-badge">SCORE / 100</span>
                </div>

                <div className="category-visuals" key={activeCategory}>
                  <ScoreChart results={results} category={activeCategory} />

                  <div className="spider-panel">
                  <div className="panel-heading spider-heading">
                    <div>
                      <h3>Visual comparison</h3>
                      <p>Exact category scores returned by the backend.</p>
                    </div>
                    <span className="score-badge">
                      {activeCategory === "OVERALL" ? "3 AXES" : "6 AXES"}
                    </span>
                  </div>
                    <SpiderChart results={results} category={activeCategory} categoryWeights={comparison?.categoryWeights} />
                  </div>
                </div>

                <div className="winner-list">
                  {results
                    .slice()
                    .sort((a, b) => (b.overallScore || 0) - (a.overallScore || 0))
                    .map((item) => (
                      <div className="winner-row" key={item.deviceId}>
                        <span className="rank">#{item.rank}</span>
                        <span>{item.deviceName}</span>
                        <b>{item.overallScore ?? "—"} pts</b>
                      </div>
                    ))}
                </div>
              </div>

              <aside className="panel category-panel">
                <h3>Compare by category</h3>
                <p>Switch the chart to inspect each use case.</p>

                <div className="category-tabs">
                  <button
                    className={
                      activeCategory === "OVERALL"
                        ? "category-tab active"
                        : "category-tab"
                    }
                    onClick={() => setActiveCategory("OVERALL")}
                  >
                    <span>Overall</span>
                    <small>All categories</small>
                  </button>
                  {CATEGORIES.map((category) => (
                    <button
                      key={category}
                      className={
                        activeCategory === category
                          ? "category-tab active"
                          : "category-tab"
                      }
                      onClick={() => setActiveCategory(category)}
                    >
                      <span>{CATEGORY_NAMES[category]}</span>
                      {activeCategory === "OVERALL" && <small>{weights[category]}%</small>}
                    </button>
                  ))}
                </div>

                {activeCategory === "OVERALL" && (
                  <div className="weight-controls">
                    <div className="weight-heading-row">
                      <div>
                        <h4>Custom weights</h4>
                      </div>
                      <strong className={weightTotal === 100 ? "weight-total valid" : "weight-total invalid"}>
                        {weightTotal}%
                      </strong>
                    </div>

                    {CATEGORIES.map((category) => (
                      <label className="weight-row" key={category}>
                        <div className="weight-row-top">
                          <span>{CATEGORY_NAMES[category]}</span>
                          <strong>{weights[category]}%</strong>
                        </div>
                        <input
                          className="weight-slider"
                          type="range"
                          min="0"
                          max="100"
                          step="1"
                          value={weights[category]}
                          style={{ "--slider-value": `${weights[category]}%` }}
                          onChange={(event) =>
                            setWeights((old) =>
                              updateCategoryWeight(old, category, event.target.value)
                            )
                          }
                          aria-label={`${CATEGORY_NAMES[category]} importance`}
                        />
                        <div className="weight-scale">
                          <span>0%</span>
                          <span>100%</span>
                        </div>
                      </label>
                    ))}

                    <button
                      className="primary-button full-button"
                      onClick={compare}
                      disabled={comparing || weightTotal !== 100}
                    >
                      {comparing ? "Updating…" : "Update comparison"}
                      <ArrowUpRight size={17} />
                    </button>
                  </div>
                )}
              </aside>
            </div>

            {aiText && (
              <div className="ai-panel">
                <div className="eyebrow">AI SUMMARY</div>
                <p>{aiText}</p>
              </div>
            )}

          </section>
        )}

        {confetti.length > 0 && (
          <div className="confetti-layer" aria-hidden="true">
            {confetti.map((piece) => (
              <span
                key={piece.id}
                className="confetti-piece"
                style={{
                  left: `${piece.left}%`,
                  backgroundColor: piece.color,
                  animationDelay: `${piece.delay}s`,
                  animationDuration: `${piece.duration}s`,
                  "--confetti-drift": `${piece.drift}px`,
                  "--confetti-rotate": `${piece.rotate}deg`,
                }}
              />
            ))}
          </div>
        )}

        <button
          type="button"
          className="confetti-trigger"
          onClick={launchConfetti}
          aria-label="Celebrate with TechCompare confetti"
          title="Celebrate"
        >
          <span className="confetti-trigger-mark">T</span>
          <span>TechCompare</span>
        </button>

        {showScrollTop && (
          <button
            type="button"
            className="scroll-top-button"
            onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}
            aria-label="Scroll to top"
            title="Back to top"
          >
            <ChevronUp size={20} />
          </button>
        )}

        <footer>
          <span>Built for better tech decisions.</span>
        </footer>
      </main>
    </div>
  );
}

createRoot(document.getElementById("root")).render(<App />);
