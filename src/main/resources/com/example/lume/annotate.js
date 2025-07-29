// Annotate the selected text with specific color
function annotate(color, annotationId) {
    let selection = window.getSelection();
    annotationId = `anno_${annotationId}`;

    if (selection && selection.rangeCount > 0 && !selection.isCollapsed) {
        /* Get the range and surround it with a span */
        let range = selection.getRangeAt(0);
        let span = document.createElement('span');
        if (color === 'red') {
            span.style.backgroundColor = 'rgba(255, 56, 83, 0.6)';
        } else if (color === 'yellow') {
            span.style.backgroundColor = 'rgba(255, 204, 0, 0.6)';
        } else if (color === 'green') {
            span.style.backgroundColor = 'rgba(0, 255, 128, 0.6)';
        } else if (color === 'cyan') {
            span.style.backgroundColor = 'rgba(139, 241, 241, 0.6)';
        }

        if (location.color) {
            span.setAttribute('data-color', location.color);
        }

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
    while (parent && !parent.id) {
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
            break;
        }

        charIndex += nodeLength;
    }

    // Check if the range is valid
    if (range.collapsed) {
        console.error("Range is collapsed - no content to annotate");
        return null;
    }

    try {
        // Create the span element
        let span = document.createElement('span');
        span.classList.add('lume-highlighted-text');
        span.id = location.annotationId;

        // Add color as a data attribute or class for CSS targeting
        if (location.color) {
            span.setAttribute('data-color', location.color);
        }

        // Try the simple approach first
        try {
            range.surroundContents(span);
            return true;
        } catch (e) {
            // Alternative approach that preserves whitespace better
            let fragment = document.createDocumentFragment();

            // Get all nodes that intersect with our range
            let iterator = document.createNodeIterator(
                range.commonAncestorContainer,
                NodeFilter.SHOW_ALL,
                {
                    acceptNode: function(node) {
                        if (range.intersectsNode(node)) {
                            return NodeFilter.FILTER_ACCEPT;
                        }
                        return NodeFilter.FILTER_REJECT;
                    }
                }
            );

            let node;
            let nodes = [];
            while (node = iterator.nextNode()) {
                if (range.intersectsNode(node) && node.nodeType === Node.TEXT_NODE) {
                    nodes.push(node);
                }
            }

            // Process each text node that intersects with our range
            for (let textNode of nodes) {
                let nodeStart = 0;
                let nodeEnd = textNode.textContent.length;

                // Calculate the intersection with our range
                let tempRange = document.createRange();
                tempRange.selectNodeContents(textNode);

                if (range.compareBoundaryPoints(Range.START_TO_START, tempRange) > 0) {
                    nodeStart = range.startOffset;
                }

                if (range.compareBoundaryPoints(Range.END_TO_END, tempRange) < 0) {
                    nodeEnd = range.endOffset;
                }

                if (nodeStart < nodeEnd) {
                    // Split the text node and wrap the middle part
                    let beforeText = textNode.textContent.substring(0, nodeStart);
                    let selectedText = textNode.textContent.substring(nodeStart, nodeEnd);
                    let afterText = textNode.textContent.substring(nodeEnd);

                    let parentElement = textNode.parentNode;

                    // Create new nodes
                    let beforeNode = beforeText ? document.createTextNode(beforeText) : null;
                    let selectedSpan = span.cloneNode();
                    selectedSpan.textContent = selectedText;
                    let afterNode = afterText ? document.createTextNode(afterText) : null;

                    // Replace the original text node
                    if (beforeNode) parentElement.insertBefore(beforeNode, textNode);
                    parentElement.insertBefore(selectedSpan, textNode);
                    if (afterNode) parentElement.insertBefore(afterNode, textNode);

                    parentElement.removeChild(textNode);

                    // Update span reference to the one we actually inserted
                    span = selectedSpan;
                    break; // For now, handle only the first intersecting node
                }
            }
        }

        return true;
    } catch (error) {
        console.error("Error creating annotation:", error);
        return false;
    }
}
