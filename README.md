Ripple Market Maker 
================

     ____  _             _        __  __            _        _     __  __       _
    |  _ \(_)_ __  _ __ | | ___  |  \/  | __ _ _ __| | _____| |_  |  \/  | __ _| | _____ _ __
    | |_) | | '_ \| '_ \| |/ _ \ | |\/| |/ _` | '__| |/ / _ \ __| | |\/| |/ _` | |/ / _ \ '__|
    |  _ <| | |_) | |_) | |  __/ | |  | | (_| | |  |   <  __/ |_  | |  | | (_| |   <  __/ |
    |_| \_\_| .__/| .__/|_|\___| |_|  |_|\__,_|_|  |_|\_\___|\__| |_|  |_|\__,_|_|\_\___|_|
            |_|   |_|

! ALPHA SOFTWARE !
this product is provided "as is" without warranty.

### What this is about ? ###
                                                                                
This project is a highly customizable experiment using Pub/Sub infrastructure for Automated Market Maker on Ripple and Bitcoin network using Spring Boot.  

Quoting Alan Kay 
    
>    The big idea is "messaging" ... The Japanese have a small word - ma - for "that which
>    is in between" - perhaps the nearest English equivalent is "interstitial".
>    The key in making great and growable systems is much more to design how its
>    modules communicate rather than what their internal properties and
>    behaviors should be. Think of the internet - to live, it (a) has to allow
>    many different kinds of ideas and realizations that are beyond any single
>    standard and (b) to allow varying degrees of safe interoperability between
>    these ideas.

* Messaging and plumbing 

We use "Redis" to support our Pub/Sub channels and Spring Boot Messaging, here is the list of our Channels:

- BOOKOFFERS

- OPPORTUNITY

- INSTRUMENTS

- OFFER

- PATH_FIND

- PATH_FIND_PAIR

### Components and process ###

We use Spring Boot and a bunch of java libraries to connect our channels and process our messages.

Here is a of COMPONENTS that connect to Ripple network, those components schedules actions and/or act as MessageListener taking action based on messages exchanged on our channels.

- RippleMarketMakerApplication

- RipplePathfindPublisher

- RipplePathfindPairPublisher

- RippexMarketMakerPathfindPair

- RippleMessageStoreListener

- RippleMessageListener

- RippleOpportunityTakerListener

### How to Build / Run / Test ###

The result is a standalone release using property file application.properties in /config directory.

    $ ./gradlew build --console plain run
    $ ./gradlew build installApp
	
### Dependencies ###

Please download and install the following softwares:

ActiveMQ 
http://activemq.apache.org/
PostgreSQL Database 
https://www.postgresql.org/
Java JDK 8 
http://www.oracle.com/technetwork/pt/java/javase/downloads/index.html

	
### Configuration ###

To override the default configuration just create a file under config/ directory called application.properties, here is an example:

ripplemm.instanceId={YOUR INSTANCE ID CREATED USING THE WEBAPP}
ripplemm.rippled=wss://s-west.ripple.com/

spring.activemq.broker-url=tcp://localhost:61616
spring.activemq.user=admin
spring.activemq.password=admin
spring.jms.pub-sub-domain=true

#spring.jpa.hibernate.ddl-auto=update
spring.jpa.generate-ddl=false

spring.jpa.database=POSTGRESQL
spring.datasource.platform=postgres
spring.database.driverClassName=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://localhost:5432/rippex
spring.datasource.username=postgres
spring.datasource.password=password

security.basic.enabled=false
management.security.enabled=false

### Authors ### 

Roberto Santacroce Martins - r@bravado.com.br
Rafael Olaio Pereira - rafael@rippex.com.br

### GOALS ###

- Use it as a playground to test algorithm trading integrated with ripple and bitcoin
- Act as a LiquidityMaker on specific market
