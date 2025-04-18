import logo from './logo.svg';
import './App.css';
import { useRef, useEffect, useState } from 'react';

function App() {
  const [resume, setResume] = useState();
  const inputRef = useRef(null);
  const [files, setFiles] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    populateData();
  }, []);

  const handleClick = () => {
    // 👇️ Open the file input box on click of another element
    inputRef.current.click();
  };

  const handleFileChange = async (e, index) => {
    const file = e.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => {
        const updatedFiles = [...files];
        updatedFiles[index] = {
            file,
            content: reader.result.split('\n'),
        };
        setFiles(updatedFiles);
    };
    reader.readAsText(file);

    const hasFile = files.some(f => f.file !== null);
    if (!hasFile) {
        setError('Please upload at least one file before submitting.');
        return;
    }

    const formData = new FormData();
    files.forEach((f, index) => {
        if (f.file) formData.append(`file${index}`, f.file);
    });

    const response = await fetch('/upload', {
        method: 'POST',
        body: formData,
    });

    if (response.ok) {
        alert('Files submitted successfully!');
        populateData(); // Fetch and show the table after successful upload
    } else {
        alert('Failed to submit files.');
    }
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
      <h1>Welcome to our home page.</h1>
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
        <input
        style={{display: 'none'}}
        ref={inputRef}
        type="file"
        onChange={handleFileChange}
      />
        </tr>
      </table>
      {/* Show error if no file uploaded */}
      {error && <p style={{ color: 'red' }}>{error}</p>}
    </div>
  );

  async function populateData() {
    const response = await fetch('resume');
    if (response.ok) {
        const data = await response.json();
        setResume(data);
    }
}
}

export default App;
