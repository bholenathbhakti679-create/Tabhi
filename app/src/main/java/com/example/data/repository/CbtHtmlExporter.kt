package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.Question
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object CbtHtmlExporter {

    fun generateStandaloneCbtHtml(
        context: Context,
        testTitle: String,
        questions: List<Question>,
        durationMinutes: Int
    ): File? {
        return try {
            val dir = File(context.filesDir, "cbt_desktop")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "tabahi_nta_cbt_test.html")

            val questionsJsonArray = JSONArray()
            questions.forEachIndexed { index, q ->
                val obj = JSONObject().apply {
                    put("id", q.id)
                    put("number", index + 1)
                    put("text", q.text)
                    put("subject", q.subject)
                    put("chapter", q.chapter)
                    put("options", JSONArray(q.options))
                    put("correctOptionIndex", q.correctOptionIndex)
                    put("isNumerical", q.isNumerical)
                    put("numericalAnswer", q.numericalAnswer)
                    put("source", q.source)
                    put("idealTimeSeconds", q.idealTimeSeconds)
                    put("solutionExplanation", q.solutionExplanation)
                }
                questionsJsonArray.put(obj)
            }

            val htmlContent = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>TABAHI - NTA CBT Simulator (${testTitle})</title>
    <style>
        :root {
            --primary: #e65100;
            --nta-blue: #1565c0;
            --answered: #10b981;
            --not-answered: #ef4444;
            --not-visited: #cbd5e1;
            --marked: #8b5cf6;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; }
        body { background: #f1f5f9; color: #1e293b; height: 100vh; display: flex; flex-direction: column; }
        header { background: #0f172a; color: white; padding: 12px 20px; display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid var(--primary); }
        .brand { font-size: 20px; font-weight: bold; color: #ff9800; display: flex; align-items: center; gap: 8px; }
        .timer-box { font-size: 18px; font-weight: bold; background: #e65100; color: white; padding: 6px 14px; border-radius: 6px; }
        .main-layout { display: flex; flex: 1; overflow: hidden; }
        .question-area { flex: 3; padding: 20px; overflow-y: auto; background: white; border-right: 2px solid #e2e8f0; display: flex; flex-direction: column; }
        .sidebar { flex: 1.2; background: #f8fafc; padding: 16px; overflow-y: auto; min-width: 280px; }
        .subject-tabs { display: flex; gap: 10px; margin-bottom: 16px; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px; }
        .tab-btn { padding: 8px 16px; border: 1px solid #cbd5e1; background: white; cursor: pointer; border-radius: 6px; font-weight: 600; }
        .tab-btn.active { background: var(--nta-blue); color: white; border-color: var(--nta-blue); }
        .q-meta { font-size: 13px; color: #64748b; margin-bottom: 12px; display: flex; justify-content: space-between; }
        .source-tag { background: #fef3c7; color: #92400e; padding: 3px 8px; border-radius: 4px; font-weight: 600; }
        .question-text { font-size: 17px; line-height: 1.6; margin-bottom: 24px; font-weight: 500; }
        .options-list { display: flex; flex-direction: column; gap: 12px; margin-bottom: 30px; }
        .option-item { display: flex; align-items: center; padding: 12px 16px; border: 2px solid #e2e8f0; border-radius: 8px; cursor: pointer; transition: all 0.2s; font-size: 15px; }
        .option-item:hover { border-color: #93c5fd; background: #f0fdf4; }
        .option-item.selected { border-color: var(--answered); background: #ecfdf5; font-weight: 600; }
        .option-item input { margin-right: 12px; transform: scale(1.2); }
        .actions-bar { margin-top: auto; padding-top: 16px; display: flex; gap: 10px; flex-wrap: wrap; border-top: 1px solid #e2e8f0; }
        .btn { padding: 10px 18px; border-radius: 6px; font-weight: 600; border: none; cursor: pointer; font-size: 14px; }
        .btn-success { background: var(--answered); color: white; }
        .btn-warning { background: var(--marked); color: white; }
        .btn-outline { background: white; border: 1px solid #cbd5e1; color: #475569; }
        .btn-submit { background: #dc2626; color: white; margin-left: auto; }
        .palette-legend { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 16px; font-size: 12px; }
        .legend-item { display: flex; align-items: center; gap: 6px; }
        .dot { width: 14px; height: 14px; border-radius: 50%; display: inline-block; }
        .palette-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; }
        .palette-btn { height: 38px; border-radius: 6px; border: 1px solid #cbd5e1; font-weight: bold; cursor: pointer; font-size: 14px; background: var(--not-visited); }
        .palette-btn.answered { background: var(--answered); color: white; border-color: var(--answered); }
        .palette-btn.not-answered { background: var(--not-answered); color: white; border-color: var(--not-answered); }
        .palette-btn.marked { background: var(--marked); color: white; border-color: var(--marked); }
        .palette-btn.current { box-shadow: 0 0 0 3px #f97316; }
        #modal { display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.6); align-items: center; justify-content: center; padding: 20px; }
        .modal-card { background: white; border-radius: 12px; max-width: 600px; width: 100%; padding: 24px; max-height: 90vh; overflow-y: auto; }
    </style>
</head>
<body>
    <header>
        <div class="brand">
            <span>🔥 TABAHI</span>
            <span style="font-size: 13px; color: #94a3b8; font-weight: normal;">| NTA Computer-Based Test Simulator</span>
        </div>
        <div class="timer-box" id="timer">Time Remaining: 00:00:00</div>
    </header>

    <div class="main-layout">
        <div class="question-area">
            <div class="subject-tabs" id="subjectTabs"></div>
            <div class="q-meta">
                <span id="qNumber">Question 1</span>
                <span class="source-tag" id="qSource">JEE Main PYQ</span>
            </div>
            <div class="question-text" id="qText"></div>
            <div class="options-list" id="optionsList"></div>
            <div class="actions-bar">
                <button class="btn btn-success" onclick="saveAndNext()">Save & Next</button>
                <button class="btn btn-warning" onclick="markForReview()">Mark for Review & Next</button>
                <button class="btn btn-outline" onclick="clearResponse()">Clear Response</button>
                <button class="btn btn-outline" onclick="prevQuestion()">Previous</button>
                <button class="btn btn-submit" onclick="confirmSubmit()">Submit Test</button>
            </div>
        </div>

        <div class="sidebar">
            <div style="font-weight: bold; margin-bottom: 12px; font-size: 15px;">Question Palette</div>
            <div class="palette-legend">
                <div class="legend-item"><span class="dot" style="background:var(--answered)"></span> Answered</div>
                <div class="legend-item"><span class="dot" style="background:var(--not-answered)"></span> Not Answered</div>
                <div class="legend-item"><span class="dot" style="background:var(--marked)"></span> Marked</div>
                <div class="legend-item"><span class="dot" style="background:var(--not-visited)"></span> Not Visited</div>
            </div>
            <div class="palette-grid" id="paletteGrid"></div>
            <div style="margin-top: 24px; padding: 12px; background: #e0f2fe; border-radius: 8px; font-size: 12px; color: #0369a1;">
                💡 <b>Tabahi Desktop Sync:</b> After submitting, copy the result token to inject back into your phone app to claim diamonds & log mistakes!
            </div>
        </div>
    </div>

    <div id="modal">
        <div class="modal-card">
            <h2 style="color: var(--primary); margin-bottom: 12px;">Test Submitted Successfully!</h2>
            <div id="modalSummary" style="font-size: 15px; line-height: 1.8; margin-bottom: 20px;"></div>
            <div style="background: #f1f5f9; padding: 12px; border-radius: 6px; margin-bottom: 16px;">
                <label style="font-size: 12px; font-weight: bold;">Tabahi Sync Token (Paste into Tabahi Mobile App):</label>
                <textarea id="syncTokenBox" style="width: 100%; height: 80px; font-family: monospace; font-size: 12px; padding: 8px; margin-top: 6px;" readonly></textarea>
            </div>
            <button class="btn btn-success" onclick="copyToken()">📋 Copy Token</button>
            <button class="btn btn-outline" onclick="closeModal()">Close & Review</button>
        </div>
    </div>

    <script>
        const rawQuestions = ${questionsJsonArray.toString()};
        let currentIndex = 0;
        let userResponses = {}; // index -> selectedOption or text
        let questionStatuses = {}; // index -> 'answered' | 'not-answered' | 'marked' | 'not-visited'
        let timeRemaining = ${durationMinutes} * 60;

        rawQuestions.forEach((_, i) => questionStatuses[i] = 'not-visited');
        questionStatuses[0] = 'not-answered';

        // Timer
        const timerEl = document.getElementById('timer');
        setInterval(() => {
            if (timeRemaining > 0) {
                timeRemaining--;
                const h = Math.floor(timeRemaining / 3600).toString().padStart(2, '0');
                const m = Math.floor((timeRemaining % 3600) / 60).toString().padStart(2, '0');
                const s = (timeRemaining % 60).toString().padStart(2, '0');
                timerEl.innerText = "Time Remaining: " + h + ":" + m + ":" + s;
            }
        }, 1000);

        function renderQuestion() {
            const q = rawQuestions[currentIndex];
            document.getElementById('qNumber').innerText = "Question " + (currentIndex + 1) + " (" + q.subject + ")";
            document.getElementById('qSource').innerText = q.source;
            document.getElementById('qText').innerText = q.text;

            const optsDiv = document.getElementById('optionsList');
            optsDiv.innerHTML = '';

            if (q.isNumerical) {
                optsDiv.innerHTML = '<input type="number" id="numInput" placeholder="Enter integer answer..." value="' + (userResponses[currentIndex] || '') + '" style="padding: 12px; font-size: 16px; border: 2px solid #cbd5e1; border-radius: 8px; width: 250px;">';
                document.getElementById('numInput').oninput = (e) => {
                    userResponses[currentIndex] = e.target.value;
                };
            } else {
                q.options.forEach((opt, oIdx) => {
                    const item = document.createElement('div');
                    item.className = 'option-item ' + (userResponses[currentIndex] === oIdx ? 'selected' : '');
                    item.innerHTML = '<input type="radio" name="opt" ' + (userResponses[currentIndex] === oIdx ? 'checked' : '') + '> ' + opt;
                    item.onclick = () => {
                        userResponses[currentIndex] = oIdx;
                        renderQuestion();
                    };
                    optsDiv.appendChild(item);
                });
            }
            renderPalette();
        }

        function renderPalette() {
            const grid = document.getElementById('paletteGrid');
            grid.innerHTML = '';
            rawQuestions.forEach((_, i) => {
                const btn = document.createElement('button');
                btn.className = 'palette-btn ' + questionStatuses[i] + (i === currentIndex ? ' current' : '');
                btn.innerText = (i + 1);
                btn.onclick = () => {
                    if (questionStatuses[currentIndex] === 'not-visited') {
                        questionStatuses[currentIndex] = 'not-answered';
                    }
                    currentIndex = i;
                    if (questionStatuses[currentIndex] === 'not-visited') {
                        questionStatuses[currentIndex] = 'not-answered';
                    }
                    renderQuestion();
                };
                grid.appendChild(btn);
            });
        }

        function saveAndNext() {
            if (userResponses[currentIndex] !== undefined && userResponses[currentIndex] !== '') {
                questionStatuses[currentIndex] = 'answered';
            } else {
                questionStatuses[currentIndex] = 'not-answered';
            }
            if (currentIndex < rawQuestions.length - 1) {
                currentIndex++;
                if (questionStatuses[currentIndex] === 'not-visited') questionStatuses[currentIndex] = 'not-answered';
            }
            renderQuestion();
        }

        function markForReview() {
            questionStatuses[currentIndex] = 'marked';
            if (currentIndex < rawQuestions.length - 1) {
                currentIndex++;
                if (questionStatuses[currentIndex] === 'not-visited') questionStatuses[currentIndex] = 'not-answered';
            }
            renderQuestion();
        }

        function clearResponse() {
            delete userResponses[currentIndex];
            questionStatuses[currentIndex] = 'not-answered';
            renderQuestion();
        }

        function prevQuestion() {
            if (currentIndex > 0) {
                currentIndex--;
                renderQuestion();
            }
        }

        function confirmSubmit() {
            if (confirm("Are you sure you want to finish and submit the test?")) {
                let correct = 0, incorrect = 0, score = 0;
                rawQuestions.forEach((q, i) => {
                    const ans = userResponses[i];
                    if (ans !== undefined && ans !== '') {
                        if (q.isNumerical) {
                            if (Math.abs(parseFloat(ans) - q.numericalAnswer) <= 0.05) {
                                correct++; score += 4;
                            } else {
                                incorrect++; score -= 1;
                            }
                        } else {
                            if (parseInt(ans) === q.correctOptionIndex) {
                                correct++; score += 4;
                            } else {
                                incorrect++; score -= 1;
                            }
                        }
                    }
                });

                const summaryHtml = "<b>Score: " + score + " / " + (rawQuestions.length * 4) + "</b><br>" +
                    "✅ Correct: " + correct + "<br>" +
                    "❌ Incorrect: " + incorrect + "<br>" +
                    "⚪ Unattempted: " + (rawQuestions.length - correct - incorrect);
                document.getElementById('modalSummary').innerHTML = summaryHtml;

                const tokenData = {
                    testTitle: "${testTitle}",
                    score: score,
                    correct: correct,
                    incorrect: incorrect,
                    date: new Date().toISOString()
                };
                document.getElementById('syncTokenBox').value = btoa(JSON.stringify(tokenData));
                document.getElementById('modal').style.display = 'flex';
            }
        }

        function copyToken() {
            const copyText = document.getElementById("syncTokenBox");
            copyText.select();
            document.execCommand("copy");
            alert("Token copied to clipboard!");
        }

        function closeModal() {
            document.getElementById('modal').style.display = 'none';
        }

        renderQuestion();
    </script>
</body>
</html>
            """.trimIndent()

            file.writeText(htmlContent)
            file
        } catch (e: Exception) {
            Log.e("CbtHtmlExporter", "Failed to generate CBT HTML", e)
            null
        }
    }
}
