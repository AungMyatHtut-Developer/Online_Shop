(() => {
    const dialog = document.getElementById('delete-user-dialog');
    const form = document.getElementById('delete-user-form');
    const cancel = document.getElementById('cancel-user-delete');
    const submit = form.querySelector('button[type="submit"]');
    let trigger = null;

    document.querySelectorAll('[data-delete-user]').forEach(link => {
        link.addEventListener('click', event => {
            // The link opens a confirmation page when dialogs are unavailable.
            if (typeof dialog.showModal !== 'function') return;
            event.preventDefault();
            trigger = link;
            form.action = link.href;
            document.getElementById('delete-user-name').textContent = link.dataset.username;
            dialog.showModal();
            cancel.focus();
        });
    });
    cancel.addEventListener('click', () => dialog.close());
    dialog.addEventListener('close', () => {
        if (trigger?.isConnected) trigger.focus();
        trigger = null;
    });
    form.addEventListener('submit', () => { submit.disabled = true; });
    window.addEventListener('pageshow', () => {
        if (dialog.open) dialog.close();
        submit.disabled = false;
    });
})();
