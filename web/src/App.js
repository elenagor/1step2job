import Clock from './Clock.js';
import './App.css';
import { useRef, useState } from 'react';
import {getResponce, getMatch} from './Request.js'

function App() {
  const inputRef = useRef(null);
  const [text, setText] = useState('');
  const [fileContent, setFileContent] = useState('');

  const handleClick = () => {
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

    setText( getResponce(fileContent) );
  };

  function OneStep() {
    return (
      <button onClick={handleClick}>  1 Step  </button>
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
