// Annotate the selected text with specific color
function annotate(color, annotationId) {
    let selection = window.getSelection();
    annotationId = `anno_${annotationId}`;

    if (selection && selection.rangeCount > 0 && !selection.isCollapsed) {
        /* Get the range and surround it with a span */
        let range = selection.getRangeAt(0);
        let span = document.createElement('span');
        span.classList.add('lume-highlighted-text');
        span.id = annotationId;

        range.surroundContents(span);

        selection.removeAllRanges();

        let loc = getSelectedTextLoc(range);
        return JSON.stringify({
            ...loc,
            color: color,
            annotationId: annotationId
        })
    }
}

// Get the location of selected text
function getSelectedTextLoc(range)
{
    let parent = range.startContainer.parentNode;
    while (parent && parent.parentNode && parent.parentNode.id) {
        parent = parent.parentNode;
    }

    if (!parent) {
        console.error("Parent not found");
        return null;
    }

    // Create a range from the parent's start to the end of the range
    const preRange = document.createRange();
    preRange.selectNodeContents(parent);
    preRange.setEnd(range.startContainer, range.startOffset);
    const start = preRange.toString().length;

    return {
        parentId: parent.id,
        start: start,
        end: start + range.toString().length
    };
}

// From the location given get the selected portion as a range for annotating
function restoreAnnotation(location) {
    const parent = document.getElementById(location.parentId);
    if (!parent) return null;

    let charIndex = 0;
    const range = document.createRange();
    let foundStart = false;

    // Use a TreeWalker to go through only the text nodes
    const walker = document.createTreeWalker(parent, NodeFilter.SHOW_TEXT, null, false);

    while (walker.nextNode()) {
        const node = walker.currentNode;
        const nodeLength = node.length;

        // If the start of the selection is in this node
        if (!foundStart && (charIndex + nodeLength) >= location.start) {
            range.setStart(node, location.start - charIndex);
            foundStart = true;
        }

        // If the end of the selection is in this node
        if (foundStart && (charIndex + nodeLength) >= location.end) {
            range.setEnd(node, location.end - charIndex);
            break; // We've found the end, so we're done
        }

        charIndex += nodeLength;
    }

    // Check if the range is valid and not collapsed
    if (range.collapsed) {
        console.error("Range is collapsed - no content to annotate");
        return null;
    }

    try {
        // Create the span element
        let span = document.createElement('span');
        span.classList.add('lume-highlighted-text');
        span.id = location.annotationId;

        // Try surroundContents first - it's cleaner when it works
        if (range.startContainer === range.endContainer &&
            range.startContainer.nodeType === Node.TEXT_NODE) {
            // Simple case: selection is within a single text node
            range.surroundContents(span);
        } else {
            // Complex case: selection spans multiple nodes or elements
            // Clone the contents instead of extracting them
            let contents = range.cloneContents();

            // Clear the original range content
            range.deleteContents();

            // Add the cloned content to our span
            span.appendChild(contents);

            // Insert the span at the range position
            range.insertNode(span);
        }

        return true; // Success
    } catch (error) {
        console.error("Error creating annotation:", error);
        return false;
    }
}
