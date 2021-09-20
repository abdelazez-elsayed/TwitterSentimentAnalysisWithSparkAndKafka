const dataPath = "/data"
let serverData;



let postiveTrace = {

    y: [],
    mode: 'lines',
    name: 'Postive',
    line: {
        color: 'rgb(100, 255, 82)',
        width: 3
    }
  };
let negativeTrace = {
   
    y: [],
    mode: 'lines',
    name: 'Negative',
    line: {
      color: 'rgb(219, 64, 82)',
      width: 3
    }
  };
  let naturalTrace = {
    
    y: [],
    mode: 'lines',
    name: 'Natural',
    line: {
      color: 'rgb(200, 200, 200)',
      width: 3
    }
  };
let data = [postiveTrace, negativeTrace,naturalTrace];

let graphDiv = document.getElementById('graph');


//My plotter
Plotly.newPlot( graphDiv, data  );

  var cnt = 0;
  var interval = setInterval(async function() {
    let serverData = await getServerData()
    console.log(serverData)
    Plotly.extendTraces(graphDiv, {
      y: [[serverData.pos], [serverData.neg] ,[serverData.nat] ]
    }, [0,1,2])
  
    cnt = cnt+1;
    if(cnt > 100) {
        Plotly.relayout(graphDiv,{
            xaxis: {
                      range: [cnt-100,cnt]
                   }
       });
    }
  }, 1500);

let getServerData = async() => {
    const res = await fetch(dataPath)
    try{
        let serverData = await res.json();
        
        console.log(serverData);
        return serverData;
    }catch(err){
        console.log("ERROR!",err);
    }
}

