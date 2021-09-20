### server.js
using kafka consumer, read from kafka prediction topic and save prediction statistics in server data object
<br /> sends this object through get route '\data'
### website 
a front end website that periodicaly takes statistics from server and display it 
## note
there a sentiment search bar but it's not working, the sentiment only works from server (application.conf)