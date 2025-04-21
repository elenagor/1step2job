import {useEffect, useState} from "react";

export default function Clock() {
    const [time, setTime] = useState(new Date().toLocaleTimeString())
    const today = new Date();
     
    function WeekDay(){
        return (  new Intl.DateTimeFormat('en-US',{ weekday: 'long' }).format(today) );
    }

    function DateDay(){
        return today.toLocaleDateString() ;
    }

    useEffect(() => {
        const id = tick()
        return () => clearTimeout(id)
    }, [])


    const tick = () => {
        return setInterval(() => {
            setTime(() => new Date().toLocaleTimeString())
        }, 1000)
    }

    return (
        <p><WeekDay/>, <DateDay/>, {time}</p>
    )
}