
export default function ToDate(){
    const today = new Date();
     
    function WeekDay(){
        return (  new Intl.DateTimeFormat('en-US',{ weekday: 'long' }).format(today) );
    }

    function DateDay(){
        return today.toLocaleDateString() ;
    }

    function DateTime(){
        return today.toLocaleTimeString() ;
    }

    return(
        <div>
            <WeekDay/>, <DateDay/>,  <DateTime/>
        </div>
    );
}