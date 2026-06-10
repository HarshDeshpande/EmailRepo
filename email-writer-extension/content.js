console.log("Email Writer Extension - Content Script Loaded");

function findComposeToolbar() {
    const selectors = [
        '.btC',
        '.aDh',
        'div[role="toolbar"]',
    ];

    for (const selector of selectors) {
        const toolbar = document.querySelector(selector);
        if (toolbar) return toolbar;
    }
    return null;
}

function getEmailContent() {
    const selectors = [
        '.h7',
        '.a3s.aiL',
        '.gmail_quote',
    ];

    for (const selector of selectors) {
        const content = document.querySelector(selector);
        if (content) return content.innerText.trim();
    }
    return '';
}

function createAIButton() {
    const button = document.createElement('button');
    button.className = 'ai-reply-button';
    button.innerHTML = 'AI Reply';
    button.setAttribute('role', 'button');
    button.setAttribute('data-tooltip', 'Generate AI Reply');

    // Custom styles (avoids Gmail class dependency)
    button.style.cssText = `
        background-color: #0b57d0;
        color: #ffffff;
        border: none;
        border-radius: 25px;
        padding: 0px 20px;
        height : 36px;
        box-sizing: border-box;     
        line-height: 32px;
        cursor: pointer;
        margin-right: 8px;
        font-size: 14px;
        font-weight: 500;
        font-family: 'Google Sans', Roboto, sans-serif;
        z-index: 9999;
    `;

    return button;
}

function injectButton() {
    const toolbar = findComposeToolbar();
    if (!toolbar) {
        console.log("Toolbar not found");
        return;
    }

    // Avoid duplicate buttons
    if (toolbar.querySelector('.ai-reply-button')) return;

    console.log("Toolbar found, injecting AI button...");
    const button = createAIButton();

    button.addEventListener('click', async () => {
        try {
            button.innerHTML = '⏳ Generating...';
            button.disabled = true;

            const emailContent = getEmailContent();
            const response = await fetch('http://localhost:8080/api/email/generator', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    emailContent: emailContent,
                    tone: "professional"
                })
            }
        );

            if (!response.ok) throw new Error('API Request Failed');

            const generatedReply = await response.text();

            const composeBox = document.querySelector('[role="textbox"][aria-label="Message Body"]')
                            || document.querySelector('.Am.Al.editable')
                            || document.querySelector('[contenteditable="true"]');

            if (composeBox) {
                composeBox.focus();
                document.execCommand('insertText', false, generatedReply);
            } else {
                console.error('ComposeBox not found');
            }

        } catch (error) {
            console.error('Error generating reply:', error);
        } finally {
            button.innerHTML = ' AI Reply';
            button.disabled = false;
        }
    });

    toolbar.insertBefore(button, toolbar.firstChild);
    console.log("AI Button Injected Successfully!");
}

// ✅ Observe DOM changes to detect compose window
const observer = new MutationObserver(() => {
    const toolbar = findComposeToolbar();
    if (toolbar && !toolbar.querySelector('.ai-reply-button')) {
        console.log("Compose Toolbar Detected");
        injectButton();
    }
});

observer.observe(document.body, {
    childList: true,
    subtree: true
});