// Setup empty JS object to act as endpoint for all routes
let data = {
    pos : 0,
    neg : 0,
    nat : 0
};
let term

const port = 8000;
// Require Express to run server and routes
const  express = require('express');
// Start up an instance of app
const app = express();
const bodyParser = require('body-parser');
/* Middleware*/
//Here we are configuring express to use body-parser as middle-ware.
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

// Cors for cross origin allowance
const cors = require('cors');
const { response } = require('express');
app.use(cors());

// Initialize the main project folder
app.use(express.static('website'));

const { Kafka } = require('kafkajs')
const kafka = new Kafka({
    clientId: 'my-app',
    brokers: ['localhost:9092']
  })
const consumer = kafka.consumer({ groupId: 'testgroup' })
//(async function(){

 consumer.connect()
consumer.subscribe({ topic: 'prediction_topic', fromBeginning: true })

 consumer.run({
  eachMessage: async ({ topic, partition, message }) => {
    console.log({
      value: message.value.toString(),

    })
    if(message.value.toString() == '2.0') // Negative 
        data.neg+=1
    else if(message.value.toString() == '1.0') //Natural
        data.nat+=1
    else                                         //Positive
        data.pos+=1
    console.log(data)
  }
})

//Get route data returns JS object
app.get('/data',(req,res) => {
    res.send(data);
})

//POST Route 
//Saves client sent data to project global object in server
app.post('/Post',(req,res) => {
    console.log(req.body);
    term = req.body.term

    res.send({response: "Data recived at the server"});
})

// Setup Server
app.listen(port,()=>{
    console.log("Server is running...");
    console.log(`Listening port ${port}`)
});