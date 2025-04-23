import './App.css';
import { useRef, useState } from 'react';
import Clock from './Clock';
import {UserProfileByResume, MatchResumeToJD} from './Request'
import {useEffect} from "react";

function App() {
  const inputRef = useRef(null);
  const [text, setText] = useState('');
  const [fileContent, setFileContent] = useState('');

  const handleOneStepButtonClick = () => {
    // 👇️ Open the file input box on click of another element
    inputRef.current.click();
  };

  const handleFileChange = async (e, index) => {
    setText('')
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
      setFileContent(e.target.result);
    };
    reader.readAsText(file);

    fetch('http://localhost:4200/user')
        .then((response) => response.json())
        .then((data) => {
          setText(data);
        })
        .catch((err) => {
          setText(err.message);
    });
  };

  function OneStep() {
    return (
      <button onClick={handleOneStepButtonClick}>  1 Step  </button>
    );
  }
  function TwoJob() {
    return (
      <button onClick={() => alert('SignIn to our smart application')} > 2 Job  </button>
    );
  }

  return (
    <div>
      <div> 
        <h1><Clock/></h1>
      </div>
      <table>
        <tr>
          <td>
            <button><OneStep/></button>
          </td>
          <td>
            <button><TwoJob/></button>
          </td>
        </tr>
        <tr>
          <input style={{display: 'none'}} ref={inputRef} type="file" onChange={handleFileChange} />
        </tr>
      </table>
      {/* Show error if no file uploaded */}
      {text && <p style={{ color: 'red' }}>{text}</p>}
    </div>
  );
}

export default App;
