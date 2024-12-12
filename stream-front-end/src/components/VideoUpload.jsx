import React, { useState } from "react";
import videoLogo from "../assets/video-posting.png";
import { Alert, Button, Card, FileInput, Label, Progress, Textarea, TextInput } from "flowbite-react";
import axios from "axios";
import toast from "react-hot-toast";

function VideoUpload(){

    const [selectedFile, setSelectedFile] = useState(null);
    const[progress, setProgress] = useState(0);
    const [meta, setMeta] = useState({
        title: "",
        description: "",
    })
    const [uploading , setUploading] = useState(false);
    const [message, setMessage] = useState("");

    function handleFileChanges(event){
        console.log(event.target.files[0]);
        setSelectedFile(event.target.files[0]);
    }
 
    function formFieldChange(event){
        
        setMeta({
            ...meta,
            [event.target.name] : event.target.value
        });
    }



    function handleForm(formEvent){
        formEvent.preventDefault();

        if(!selectedFile)
        {
            alert("Select the File !!!");
            return;
        }


        // console.log(formEvent.target.title.value);
        // console.log(formEvent.target.description.value);
    
        // submit file to the server
        saveVideoToServer(selectedFile, meta);

    }



function resetForm(){
    setMeta({
        title: "",
        description : "",
    });
    setSelectedFile(null);
    setUploading(false);
   // setMessage("");
}




    // submit file to server
async function saveVideoToServer(video,videoMetaData){
    setUploading(true);
    
    // api call
    try{

        let formData = new FormData();
        formData.append("title", videoMetaData.title);
        formData.append("description", videoMetaData.description);
        formData.append("file", selectedFile);

     let response=   await axios.post(`http://localhost:8080/api/v1/videos`, formData,{
            headers: {
                'Content-Type': 'multipart/form-data'
            },
            onUploadProgress : (progressEvent)=>{
                const progress = Math.round((progressEvent.loaded*100)/progressEvent.total);
                
                setProgress((progress));

                console.log(progress);
                //console.log(progressEvent);
            }
        });
        console.log(response);

        
        setMessage("   File Uploaded "+ response.data.videoId);

        setUploading(false);
        toast.success("File uploaded successfully...");
        resetForm();

    }catch(error)
    {
        console.log(error);
        setMessage("  Error in Uploading file  ");

        setUploading(false);
        toast.error("Error in Uploading file...")
    }
}


    return <div className="text-white"> 
    
    <Card className="flex flex-col items-center ">
        <h1>
            Upload Videos
        </h1>

       <div >

{/* form start here */}
 <form noValidate
        onSubmit={handleForm}
        className="flex flex-col space-y-6">
{/* This is input title field */}
<div>
      <div className="mb-2 block">
        <Label htmlFor="file-upload" value="Video Title" />
      </div>
      <TextInput value={meta.title}   onChange={formFieldChange} name="title" placeholder="Enter Title" />
    </div>

    {/* This is input text area field */}

    <div className="max-w-md">
      <div className="mb-2 block">
        <Label htmlFor="comment" value="Video Description" />
      </div>
      <Textarea value={meta.description} onChange={formFieldChange} name="description" id="comment" placeholder="write video description..." required rows={4} />
    </div>

    {/* This is upload file field */}

            <div   className="flex items-center space-x-5 justify-center">
  
    <div className="shrink-0">
      <img className="h-16 w-16 object-cover " src={videoLogo} />
    </div>
    <label className="block">
      <span className="sr-only">Choose Video file</span>
      <input 
      
      name="file"
      onChange={handleFileChanges}
      type="file" class="block w-full text-sm text-slate-500
        file:mr-4 file:py-2 file:px-4
        file:rounded-full file:border-0
        file:text-sm file:font-semibold
        file:bg-violet-50 file:text-violet-700
        hover:file:bg-violet-100
      "/>
    </label>

    </div>

    <div className="">
       {uploading && (
         <Progress 
        
         progress={progress} textLabel="Uploading..." size="lg" labelProgress labelText />
            
       )}
    </div>

    <div className="">
    {message && (
        <Alert  color="success" rounded withBorderAccent onDismiss={()=>{
            setMessage("");
            }}>
        <span className="font-medium">success alert !!    </span>
         {message}
      </Alert>
    )}
    </div>

    <div className="flex justify-center">
        <Button disabled={uploading} type="submit" >Upload</Button>
    </div>

  </form>
       </div>
      
    </Card>
    
    </div>;

    
}

export default VideoUpload;

