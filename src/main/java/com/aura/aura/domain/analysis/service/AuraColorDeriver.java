package com.aura.aura.domain.analysis.service;

import com.aura.aura.domain.analysis.entity.Mood;

import java.util.Map;

public final class AuraColorDeriver {

    private static final Map<Mood, float[]> METAL_BASE = Map.of(
            Mood.CLASSIC,  new float[]{ 42f, 0.45f, 0.62f },
            Mood.ROMANTIC, new float[]{ 15f, 0.42f, 0.68f },
            Mood.STREET,   new float[]{210f, 0.10f, 0.74f },
            Mood.MINIMAL,  new float[]{210f, 0.05f, 0.80f }
    );

    private static final float HUE_PULL = 0.20f;
    private static final float MIN_CONTRAST_MAIN = 0.25f;
    private static final float MIN_CONTRAST_PATTERN = 0.18f;

    private AuraColorDeriver() {
    }

    public static String deriveAccent(String mainHex, String patternHex, Mood mood) {
        float[] main = hexToHsl(mainHex);
        float[] pattern = hexToHsl(patternHex);
        float[] metal = METAL_BASE.getOrDefault(mood, METAL_BASE.get(Mood.CLASSIC));

        float hue = lerpHue(metal[0], main[0], HUE_PULL);
        float saturation = metal[1] * (0.75f + 0.25f * main[1]);
        float lightness = resolveLightness(metal[2], main[2], pattern[2]);

        return hslToHex(hue, saturation, lightness);
    }

    private static float resolveLightness(float base, float mainL, float patternL) {
        float avg = (mainL + patternL) / 2f;

        float target = (avg < 0.5f)
                ? Math.max(base, avg + MIN_CONTRAST_MAIN)
                : Math.min(base, avg - MIN_CONTRAST_MAIN);

        target = pushAway(target, mainL, MIN_CONTRAST_MAIN);
        target = pushAway(target, patternL, MIN_CONTRAST_PATTERN);

        return clamp(target, 0.18f, 0.88f);
    }

    private static float pushAway(float value, float other, float minGap) {
        float gap = value - other;
        if (Math.abs(gap) >= minGap) return value;
        return (gap >= 0) ? other + minGap : other - minGap;
    }

    public static float[] hexToHsl(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);

        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float lightness = (max + min) / 2f;
        float hue = 0f;
        float saturation = 0f;

        if (delta != 0f) {
            saturation = (lightness > 0.5f)
                    ? delta / (2f - max - min)
                    : delta / (max + min);

            if (max == rf) {
                hue = ((gf - bf) / delta) + (gf < bf ? 6f : 0f);
            } else if (max == gf) {
                hue = ((bf - rf) / delta) + 2f;
            } else {
                hue = ((rf - gf) / delta) + 4f;
            }
            hue /= 6f;
        }

        return new float[]{ hue * 360f, saturation, lightness };
    }

    public static String hslToHex(float hue, float saturation, float lightness) {
        float h = (((hue % 360f) + 360f) % 360f) / 360f;
        float s = clamp(saturation, 0f, 1f);
        float l = clamp(lightness, 0f, 1f);

        float r, g, b;
        if (s == 0f) {
            r = g = b = l;
        } else {
            float q = (l < 0.5f) ? l * (1f + s) : l + s - l * s;
            float p = 2f * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }

        return String.format("#%02X%02X%02X",
                Math.round(r * 255f), Math.round(g * 255f), Math.round(b * 255f));
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0f) t += 1f;
        if (t > 1f) t -= 1f;
        if (t < 1f / 6f) return p + (q - p) * 6f * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    private static float lerpHue(float from, float to, float ratio) {
        float diff = ((to - from + 540f) % 360f) - 180f;
        return ((from + diff * ratio) % 360f + 360f) % 360f;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
