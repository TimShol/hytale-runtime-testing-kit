package com.frotty27.hrtk.server.result;

import com.frotty27.hrtk.api.lifecycle.SuiteResult;
import com.frotty27.hrtk.api.lifecycle.TestResult;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ResultExporter {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ResultExporter() {}

    public static Path exportToJson(Path directory, List<SuiteResult> results) throws IOException {
        Files.createDirectories(directory);
        String filename = "run_" + System.currentTimeMillis() + ".json";
        Path file = directory.resolve(filename);

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": ").append(System.currentTimeMillis()).append(",\n");
        json.append("  \"suites\": [\n");

        for (int suiteIndex = 0; suiteIndex < results.size(); suiteIndex++) {
            SuiteResult suite = results.get(suiteIndex);
            json.append("    {\n");
            json.append("      \"plugin\": ").append(jsonString(suite.getPluginName())).append(",\n");
            json.append("      \"suite\": ").append(jsonString(suite.getSuiteName())).append(",\n");
            json.append("      \"durationMs\": ").append(suite.getTotalDurationMs()).append(",\n");
            json.append("      \"tests\": [\n");

            List<TestResult> tests = suite.getResults();
            for (int testIndex = 0; testIndex < tests.size(); testIndex++) {
                TestResult test = tests.get(testIndex);
                json.append("        {\n");
                json.append("          \"name\": ").append(jsonString(test.getTestName())).append(",\n");
                json.append("          \"displayName\": ").append(jsonString(test.getDisplayName())).append(",\n");
                json.append("          \"status\": ").append(jsonString(test.getStatus().name())).append(",\n");
                json.append("          \"durationMs\": ").append(test.getDurationMs()).append(",\n");
                json.append("          \"message\": ").append(test.getMessage() != null ? jsonString(test.getMessage()) : "null").append(",\n");
                json.append("          \"tags\": [");
                for (int i = 0; i < test.getTags().size(); i++) {
                    if (i > 0) json.append(", ");
                    json.append(jsonString(test.getTags().get(i)));
                }
                json.append("]\n");
                json.append("        }").append(testIndex < tests.size() - 1 ? "," : "").append('\n');
            }

            json.append("      ]\n");
            json.append("    }").append(suiteIndex < results.size() - 1 ? "," : "").append('\n');
        }

        json.append("  ]\n");
        json.append("}\n");

        Files.writeString(file, json.toString());
        LOGGER.atInfo().log("HRTK: Results exported to %s", file);
        return file;
    }

    public static Path exportToHtml(Path directory, List<SuiteResult> results) throws IOException {
        Files.createDirectories(directory);
        long timestamp = System.currentTimeMillis();
        String filename = "run_" + timestamp + ".html";
        Path file = directory.resolve(filename);

        long totalPassed = 0, totalFailed = 0, totalSkipped = 0, totalErrored = 0;
        long totalDuration = 0;
        for (SuiteResult suite : results) {
            totalPassed += suite.countPassed();
            totalFailed += suite.countFailed();
            totalSkipped += suite.countSkipped();
            totalDuration += suite.getTotalDurationMs();
            for (TestResult test : suite.getResults()) {
                if (test.getStatus().name().equals("ERRORED")) totalErrored++;
            }
        }
        long totalTests = totalPassed + totalFailed + totalSkipped + totalErrored;
        String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(timestamp));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">");
        html.append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">");
        html.append("<title>HRTK Test Results</title>");
        html.append("<link rel=\"icon\" type=\"image/svg+xml\" href=\"data:image/svg+xml,");
        html.append("%3Csvg viewBox='0 0 32 32' xmlns='http://www.w3.org/2000/svg'%3E");
        html.append("%3Crect x='1' y='1' width='30' height='30' rx='7' fill='%231E293B' stroke='%232563EB' stroke-width='2'/%3E");
        html.append("%3Cpolyline points='9,17 13.5,21.5 23,10' stroke='%2322C55E' stroke-width='3' fill='none' stroke-linecap='round' stroke-linejoin='round'/%3E");
        html.append("%3C/svg%3E\">");
        html.append("<style>");
        html.append(":root{--bg:#f8fafc;--bg2:#ffffff;--bg3:#e2e8f0;--fg:#0f172a;--fg2:#475569;--fg3:#94a3b8;--border:#e2e8f0;--hover:#f1f5f9}");
        html.append(".dark{--bg:#0f172a;--bg2:#1e293b;--bg3:#334155;--fg:#e2e8f0;--fg2:#94a3b8;--fg3:#64748b;--border:#1a2332;--hover:#253348}");
        html.append("*{margin:0;padding:0;box-sizing:border-box}");
        html.append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:var(--bg);color:var(--fg);padding:2rem;transition:background .3s,color .3s}");
        html.append(".header{text-align:center;margin-bottom:2rem}");
        html.append(".logo{display:inline-flex;align-items:center;justify-content:center;gap:.6rem;margin-bottom:.5rem}");
        html.append(".logo svg{width:28px;height:28px;flex-shrink:0;position:relative;top:2px}");
        html.append(".logo h1{font-size:1.6rem;line-height:28px}");
        html.append(".date{color:var(--fg2);font-size:.9rem}");
        html.append(".theme-toggle{position:fixed;top:1rem;right:1rem;background:var(--bg2);border:1px solid var(--bg3);color:var(--fg);border-radius:8px;padding:.5rem .75rem;cursor:pointer;font-size:.85rem;transition:all .3s}");
        html.append(".theme-toggle:hover{background:var(--hover)}");
        html.append(".summary{display:flex;gap:1rem;justify-content:center;margin:1.5rem 0;flex-wrap:wrap}");
        html.append(".stat{background:var(--bg2);border-radius:8px;padding:1rem 1.5rem;text-align:center;min-width:120px;transition:background .3s}");
        html.append(".stat .num{font-size:2rem;font-weight:700}");
        html.append(".stat .label{font-size:.8rem;color:var(--fg2);text-transform:uppercase;margin-top:.25rem}");
        html.append(".pass .num{color:#22c55e}.fail .num{color:#ef4444}.skip .num{color:#f59e0b}.err .num{color:#f97316}");
        html.append(".bar{height:8px;border-radius:4px;background:var(--bg2);margin:1rem auto;max-width:600px;overflow:hidden;display:flex}");
        html.append(".bar .seg-pass{background:#22c55e}.bar .seg-fail{background:#ef4444}.bar .seg-skip{background:#f59e0b}.bar .seg-err{background:#f97316}");
        html.append(".suite{background:var(--bg2);border-radius:8px;margin:1rem 0;overflow:hidden;transition:background .3s}");
        html.append(".suite-header{padding:.75rem 1rem;font-weight:600;display:flex;justify-content:space-between;align-items:center;cursor:pointer;border-bottom:1px solid var(--border);user-select:none}");
        html.append(".suite-header:hover{background:var(--hover)}");
        html.append(".suite-header .arrow{transition:transform .2s;font-size:.7rem;color:var(--fg3)}");
        html.append(".suite-header.collapsed .arrow{transform:rotate(-90deg)}");
        html.append(".suite-body{padding:0;overflow:hidden;transition:max-height .3s}");
        html.append(".suite-body.hidden{max-height:0!important}");
        html.append(".test{display:flex;align-items:center;padding:.5rem 1rem;border-bottom:1px solid var(--border);gap:.75rem}");
        html.append(".test:last-child{border-bottom:none}");
        html.append(".badge{font-size:.7rem;font-weight:700;padding:.15rem .5rem;border-radius:4px;text-transform:uppercase;min-width:48px;text-align:center}");
        html.append(".badge-pass{background:#dcfce7;color:#166534}.badge-fail{background:#fee2e2;color:#991b1b}");
        html.append(".badge-skip{background:#fef3c7;color:#92400e}.badge-err{background:#ffedd5;color:#9a3412}");
        html.append(".dark .badge-pass{background:#166534;color:#22c55e}.dark .badge-fail{background:#7f1d1d;color:#ef4444}");
        html.append(".dark .badge-skip{background:#78350f;color:#f59e0b}.dark .badge-err{background:#7c2d12;color:#f97316}");
        html.append(".test-name{flex:1;font-size:.9rem}.test-dur{color:var(--fg3);font-size:.8rem}");
        html.append(".test-msg{color:#dc2626;font-size:.8rem;padding:.25rem 1rem .5rem 4.5rem;white-space:pre-wrap;font-family:monospace}");
        html.append(".dark .test-msg{color:#f87171}");
        html.append(".stat{cursor:pointer;user-select:none;border:2px solid transparent;transition:all .3s}");
        html.append(".stat.active{border-color:var(--fg)}");
        html.append(".suite.filter-hidden{display:none}");
        html.append(".test.filter-hidden{display:none}");
        html.append(".stacktrace{display:none;margin:0 1rem .5rem 1rem;padding:.75rem;background:var(--bg);border-radius:6px;border-left:3px solid #ef4444;font-family:'JetBrains Mono',Consolas,'Courier New',monospace;font-size:.75rem;line-height:1.6;overflow-x:auto;white-space:pre;color:var(--fg3)}");
        html.append(".stacktrace.visible{display:block}");
        html.append(".stacktrace .st-err{color:#ef4444;font-weight:700}");
        html.append(".stacktrace .st-cause{color:#f97316;font-weight:600}");
        html.append(".stacktrace .st-user{color:var(--fg);font-weight:500}");
        html.append(".stacktrace .st-user .st-line{text-decoration:wavy underline #ef4444;text-underline-offset:3px}");
        html.append(".stacktrace .st-framework{color:#60a5fa}");
        html.append(".stacktrace .st-hytale{color:var(--fg3)}");
        html.append(".test-details{display:none}");
        html.append(".test-details.visible{display:block}");
        html.append(".test-details .test-msg{padding:.5rem 1rem .25rem 4rem}");
        html.append(".test-expandable{cursor:pointer}");
        html.append(".test-expandable:hover{background:var(--hover)}");
        html.append(".plugin{color:var(--fg2);font-size:.8rem}");
        html.append(".duration{color:var(--fg3);font-size:.85rem}");
        html.append(".suite-counts{display:flex;gap:.5rem;align-items:center}");
        html.append(".suite-counts .mini{font-size:.75rem;font-weight:600}");
        html.append(".mini-pass{color:#22c55e}.mini-fail{color:#ef4444}.mini-skip{color:#f59e0b}");
        html.append("</style></head><body>");

        html.append("<button class=\"theme-toggle\" onclick=\"toggleTheme()\">Dark Mode</button>");

        html.append("<div class=\"header\">");
        html.append("<div class=\"logo\">");
        html.append("<svg viewBox=\"0 0 32 32\" xmlns=\"http://www.w3.org/2000/svg\">");
        html.append("<rect x=\"1\" y=\"1\" width=\"30\" height=\"30\" rx=\"7\" fill=\"#1E293B\" stroke=\"#2563EB\" stroke-width=\"2\"/>");
        html.append("<polyline points=\"9,17 13.5,21.5 23,10\" stroke=\"#22C55E\" stroke-width=\"3\" fill=\"none\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>");
        html.append("</svg>");
        html.append("<h1>HRTK Test Results</h1>");
        html.append("</div>");
        html.append("<div class=\"date\">").append(htmlEscape(dateStr)).append(" - ").append(totalDuration).append("ms total</div>");
        html.append("</div>");

        html.append("<div class=\"summary\">");
        html.append("<div class=\"stat pass\" onclick=\"filterBy('PASSED')\" data-filter=\"PASSED\"><div class=\"num\">").append(totalPassed).append("</div><div class=\"label\">Passed</div></div>");
        html.append("<div class=\"stat fail\" onclick=\"filterBy('FAILED')\" data-filter=\"FAILED\"><div class=\"num\">").append(totalFailed).append("</div><div class=\"label\">Failed</div></div>");
        html.append("<div class=\"stat err\" onclick=\"filterBy('ERRORED')\" data-filter=\"ERRORED\"><div class=\"num\">").append(totalErrored).append("</div><div class=\"label\">Errored</div></div>");
        html.append("<div class=\"stat skip\" onclick=\"filterBy('SKIPPED')\" data-filter=\"SKIPPED\"><div class=\"num\">").append(totalSkipped).append("</div><div class=\"label\">Skipped</div></div>");
        html.append("</div>");

        if (totalTests > 0) {
            html.append("<div class=\"bar\">");
            html.append("<div class=\"seg-pass\" style=\"width:").append(totalPassed * 100 / totalTests).append("%\"></div>");
            html.append("<div class=\"seg-fail\" style=\"width:").append(totalFailed * 100 / totalTests).append("%\"></div>");
            html.append("<div class=\"seg-err\" style=\"width:").append(totalErrored * 100 / totalTests).append("%\"></div>");
            html.append("<div class=\"seg-skip\" style=\"width:").append(totalSkipped * 100 / totalTests).append("%\"></div>");
            html.append("</div>");
        }

        for (SuiteResult suite : results) {
            long suitePassed = suite.countPassed(), suiteFailed = suite.countFailed(), suiteSkipped = suite.countSkipped();

            html.append("<div class=\"suite\">");
            html.append("<div class=\"suite-header\" onclick=\"toggleSuite(this)\">");
            html.append("<span><span class=\"arrow\">&#9660;</span> ").append(htmlEscape(suite.getSuiteName()));
            html.append(" <span class=\"plugin\">").append(htmlEscape(suite.getPluginName())).append("</span></span>");
            html.append("<span class=\"suite-counts\">");
            if (suitePassed > 0) html.append("<span class=\"mini mini-pass\">").append(suitePassed).append(" pass</span>");
            if (suiteFailed > 0) html.append("<span class=\"mini mini-fail\">").append(suiteFailed).append(" fail</span>");
            if (suiteSkipped > 0) html.append("<span class=\"mini mini-skip\">").append(suiteSkipped).append(" skip</span>");
            html.append("<span class=\"duration\">").append(suite.getTotalDurationMs()).append("ms</span>");
            html.append("</span></div>");
            html.append("<div class=\"suite-body\">");

            for (TestResult test : suite.getResults()) {
                String status = test.getStatus().name();
                String badgeClass = switch (status) {
                    case "PASSED" -> "badge-pass";
                    case "FAILED" -> "badge-fail";
                    case "SKIPPED" -> "badge-skip";
                    default -> "badge-err";
                };
                String badgeLabel = switch (status) {
                    case "PASSED" -> "PASS";
                    case "FAILED" -> "FAIL";
                    case "SKIPPED" -> "SKIP";
                    case "TIMED_OUT" -> "TIME";
                    default -> "ERR";
                };

                boolean hasStack = test.getStackTrace() != null && !test.getStackTrace().isEmpty();
                boolean hasDetails = hasStack || (test.getMessage() != null
                        && (test.failed() || "ERRORED".equals(status) || "SKIPPED".equals(status)));
                String expandClass = hasDetails ? " test-expandable" : "";
                String expandClick = hasDetails ? " onclick=\"toggleStack(this)\"" : "";

                html.append("<div class=\"test").append(expandClass).append("\" data-status=\"").append(status).append("\"").append(expandClick).append(">");
                html.append("<span class=\"badge ").append(badgeClass).append("\">").append(badgeLabel).append("</span>");
                html.append("<span class=\"test-name\">").append(htmlEscape(test.getDisplayName()));
                if (hasDetails) html.append(" <span style=\"font-size:.7rem;color:var(--fg3)\">&#9654;</span>");
                html.append("</span>");
                html.append("<span class=\"test-dur\">").append(test.getDurationMs()).append("ms</span>");
                html.append("</div>");

                if (hasDetails) {
                    html.append("<div class=\"test-details\" data-status=\"").append(status).append("\">");

                    if (test.getMessage() != null) {
                        html.append("<div class=\"test-msg\" data-status=\"").append(status).append("\">").append(htmlEscape(test.getMessage())).append("</div>");
                    }

                    if (hasStack) {
                        html.append("<div class=\"stacktrace\">");
                        html.append(formatStackTraceHtml(test.getStackTrace()));
                        html.append("</div>");
                    }

                    html.append("</div>");
                }
            }

            html.append("</div></div>");
        }

        html.append("<script>");
        html.append("let activeFilter=null;");
        html.append("function toggleTheme(){document.body.classList.toggle('dark');");
        html.append("document.querySelector('.theme-toggle').textContent=document.body.classList.contains('dark')?'Light Mode':'Dark Mode'}");
        html.append("function toggleSuite(el){el.classList.toggle('collapsed');el.nextElementSibling.classList.toggle('hidden')}");
        html.append("function toggleStack(el){var d=el.nextElementSibling;if(d&&d.classList.contains('test-details')){");
        html.append("d.querySelectorAll('.stacktrace').forEach(s=>s.classList.toggle('visible'));");
        html.append("d.classList.toggle('visible')}}");
        html.append("function filterBy(status){");
        html.append("document.querySelectorAll('.stat').forEach(s=>s.classList.remove('active'));");
        html.append("if(activeFilter===status){activeFilter=null}else{activeFilter=status;document.querySelector('.stat[data-filter=\"'+status+'\"]').classList.add('active')}");
        html.append("document.querySelectorAll('.test').forEach(t=>{");
        html.append("let s=t.getAttribute('data-status');");
        html.append("let hide=activeFilter&&s!==activeFilter;");
        html.append("t.classList.toggle('filter-hidden',hide);");
        html.append("let next=t.nextElementSibling;if(next&&(next.classList.contains('test-msg')||next.classList.contains('test-details')))next.classList.toggle('filter-hidden',hide)});");
        html.append("document.querySelectorAll('.suite').forEach(suite=>{");
        html.append("let tests=suite.querySelectorAll('.test:not(.filter-hidden)');");
        html.append("suite.classList.toggle('filter-hidden',activeFilter&&tests.length===0)});");
        html.append("}");
        html.append("</script>");
        html.append("</body></html>");

        Files.writeString(file, html.toString());
        LOGGER.atInfo().log("HRTK: HTML report exported to %s", file);
        return file;
    }

    private static String formatStackTraceHtml(String stackTrace) {
        StringBuilder output = new StringBuilder();
        for (String line : stackTrace.split("\n")) {
            String trimmed = line.trim();
            String escaped = htmlEscape(line);

            if (trimmed.startsWith("Caused by:") || trimmed.startsWith("Suppressed:")) {
                output.append("<span class=\"st-cause\">").append(escaped).append("</span>\n");
            } else if (trimmed.startsWith("at ") && isUserFrame(trimmed)) {
                int parenStart = escaped.indexOf('(');
                int parenEnd = escaped.indexOf(')');
                if (parenStart > 0 && parenEnd > parenStart) {
                    output.append("<span class=\"st-user\">");
                    output.append(escaped, 0, parenStart + 1);
                    output.append("<span class=\"st-line\">");
                    output.append(escaped, parenStart + 1, parenEnd);
                    output.append("</span>");
                    output.append(escaped.substring(parenEnd));
                    output.append("</span>\n");
                } else {
                    output.append("<span class=\"st-user\">").append(escaped).append("</span>\n");
                }
            } else if (trimmed.startsWith("at com.frotty27.hrtk.")) {
                output.append("<span class=\"st-framework\">").append(escaped).append("</span>\n");
            } else if (trimmed.startsWith("at com.hypixel.")) {
                output.append("<span class=\"st-hytale\">").append(escaped).append("</span>\n");
            } else if (!trimmed.startsWith("at ") && !trimmed.startsWith("...")) {
                output.append("<span class=\"st-err\">").append(escaped).append("</span>\n");
            } else {
                output.append(escaped).append("\n");
            }
        }
        return output.toString();
    }

    private static boolean isUserFrame(String line) {
        return !line.contains("com.frotty27.hrtk.")
                && !line.contains("com.hypixel.")
                && !line.contains("java.")
                && !line.contains("jdk.")
                && !line.contains("sun.")
                && !line.contains("ForkJoinPool")
                && !line.contains("CompletableFuture");
    }

    private static String htmlEscape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    static String jsonString(String value) {
        if (value == null) return "null";
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
}
