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

function SpiderChart({ results, category }) {
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
