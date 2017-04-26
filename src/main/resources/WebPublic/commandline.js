"use strict";


// this will be filled in by window.onload
var commandLineElement = ""

// this doesn't happen until the DOM is instantiated
window.onload = function() {
    printParagraph("<i><u>commandLine initialized, ready to rock</u></i>")

    snack.wrap(document.getElementById('goButton'))
        .each(function (element, index) {
            var params = {node: element, event: 'click'}
            snack.listener(params, function (event) {
                fetchQuery()
            })
        })

    snack.wrap(document.getElementById('commandLine'))
        .each(function (element, index) {
            var params = {node: element, event: 'keydown'}
            commandLineElement = element;
            snack.listener(params, function (event) {
                if(event.keyCode == 13) {
                    // the user hit return
                    snack.preventDefault(event) // prevents the carriage-return from triggering a page reload
                    fetchQuery()
                }
            })
        })
}


function fetchQuery() {
    var savedText = commandLineElement.value
    commandLineElement.value = ''

    dispatchQuery(savedText)
}

function printParagraph(text) {
    printRaw("<p>" + text + "</p>");
}

function printRaw(text) {
    var textBox = document.getElementById('textOutput')
    textBox.innerHTML += text;
    textBox.scrollTop = textBox.scrollHeight;
}

function dispatchQuery(input) {
    var options = {
        method: 'get',
        url: '/lowercase/',
        data: {'input': input}
    }


    snack.request(options, function (err, res){
        // check for an error
        if (err) {
            printParagraph('<b>Bah! ' + err + ' error!</b>')
            return
        }

        // no error
        handleResponse(res)
    })
}

function handleResponse(data) {
    var result = JSON.parse(data).response
    printParagraph(result)
}
