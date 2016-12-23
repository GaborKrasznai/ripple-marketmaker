/*global define: true, metaproject: true, ko: true, $: true */
define(function (require) {
    "use strict";

    // Module dependencies
    var Boiler = require('Boiler'),
        menuTemplate = require('text!./menu.html'),
        viewTemplate = require('text!./view.html');

    var viewModel = {
        name: 'Rippex Arbitrager Monitor',
        url: '',
        messages : ko.observableArray(),
        currentMessage : ko.observable(),
        exchanges : ko.observableArray(),
        selectedMessage : ko.observable(),
        pathfindPair : ko.observable(),
        pathfinds : ko.observableArray(),
        clickMessage : function(data) {
        	var value = JSON.stringify(ko.mapping.toJS(data), undefined, 2);
        	viewModel.selectedMessage(value);
        	$('#messageDetail pre').jJsonViewer(value);
        	$('#messageDetail').modal('show');
        },
        payments : ko.observableArray(),
        clickPayment : function(data) {
        console.log(JSON.parse(data.content()));
        	$('#messageDetail pre').jJsonViewer(JSON.stringify(data.content()));
        	$('#messageDetail').modal('show');
        },
        getExchangeCurrencySourceAmount : function(message, key, p) {
        	var val = '';
        	$.each(message.exchanges(), function(k, v) {
        		if(key === v.key()) {
        		console.log(v.pathfind.pathfind1.alternative.source_amount.currency);
	        		if(p === 'pathfind1') {
	        			val = v.pathfind.pathfind1.alternative.source_amount.currency() + ' ' + v.pathfind.pathfind1.alternative.source_amount.value();
	        		} else {
	        			val = v.pathfind.pathfind2.alternative.source_amount.currency() + ' ' + v.pathfind.pathfind2.alternative.source_amount.value();
	        		}
	        		return val;
        		} 
        	});
        	return val;
        }
    };

    var stompClient = null;
    var socket; 
        
    var connect = function connect() {
            socket = new SockJS('/hello');
            stompClient = Stomp.over(socket);            
            stompClient.connect({}, function(frame) {
                console.log('Connected: ' + frame);
                
                // payments
                  stompClient.subscribe('/topic/payments', function(message){
               		var msg = ko.mapping.fromJSON(message.body);
               		if(viewModel.payments.length > 100) {
                    	viewModel.payments(ko.observableArray());
                    }
                    viewModel.payments.unshift(msg);
                });
                
                stompClient.subscribe('/topic/pathfinds', function(message){
               		var msg = ko.mapping.fromJSON(message.body);
               		if(viewModel.pathfinds.length > 25) {
                    	viewModel.pathfinds(ko.observableArray());
                    }
                    viewModel.pathfinds.unshift(msg);
                    viewModel.pathfindPair = msg;
                });
               	
                stompClient.subscribe('/topic/greetings', function(message){
                    var msg = ko.mapping.fromJSON(message.body);
                    if(viewModel.messages().length >= 50) {
                    	viewModel.messages.removeAll();
                    }
                    viewModel.messages.unshift(msg);
                    viewModel.exchanges = msg.exchanges();
                });

                stompClient.send("/app/hello", {}, {});    
                stompClient.send("/app/pathfind", {}, {});
            });
    }
        
    var disconnect = function disconnect() {
        if (stompClient != null) {
            stompClient.disconnect();
        }
        console.log("Disconnected");
    }
    
    $('.sidebar-menu').prepend(menuTemplate);

    return {
        template: viewTemplate,
        viewModel: viewModel,
        activate: function(parent, params) {
            connect();
        },
        deactivate: function() {
            disconnect();
        }

    };

});
