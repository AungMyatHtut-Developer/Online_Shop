(() => {
    const dialog = document.getElementById('delete-product-dialog');
    const productName = document.getElementById('delete-product-name');
    const cancelButton = document.getElementById('cancel-product-delete');
    const confirmButton = document.getElementById('confirm-product-delete');
    let pendingForm = null;
    let triggerButton = null;

    document.querySelectorAll('[data-delete-product]').forEach((button) => {
        button.addEventListener('click', () => {
            pendingForm = button.form;
            triggerButton = button;
            productName.textContent = button.dataset.productName;
            confirmButton.disabled = false;
            confirmButton.textContent = 'Delete product';
            dialog.showModal();
            cancelButton.focus();
        });
    });

    cancelButton.addEventListener('click', () => dialog.close());

    // Closing with Cancel or Escape returns focus without submitting a form.
    dialog.addEventListener('close', () => {
        pendingForm = null;
        if (triggerButton && triggerButton.isConnected) triggerButton.focus();
        triggerButton = null;
    });

    confirmButton.addEventListener('click', () => {
        if (!pendingForm || confirmButton.disabled) return;
        confirmButton.disabled = true;
        confirmButton.textContent = 'Deleting...';
        // Submit the selected row's existing POST form, including its session token.
        HTMLFormElement.prototype.submit.call(pendingForm);
    });

    // Reset a pending confirmation when returning via the browser's Back button.
    window.addEventListener('pageshow', () => {
        if (dialog.open) dialog.close();
        pendingForm = null;
        confirmButton.disabled = false;
        confirmButton.textContent = 'Delete product';
    });
})();
