
export default function ToDate(){
    const today = new Date();
    let options = {hour: 'numeric', minute: 'numeric', dayPeriod: 'short'};
     
    function WeekDay(){
        return (  new Intl.DateTimeFormat('en-US',{ weekday: 'long' }).format(today) );
    }

    function DateDay(){
        return (  new Intl.DateTimeFormat('en-US',{ options }).format(today) );
    }

    return(
        <div>
            Today is <DateDay/>, <WeekDay/>
        </div>
    );
}