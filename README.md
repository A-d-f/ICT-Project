# ICT-Project

**ICT project made for Häme University of Applied Sciences and Cinia Oy.**  
**The project goal was to create a proof of concept - how could speech recognition be utilized in emergency call processes.**

Team:  [Anna-Maria Palm](https://github.com/A-d-f), [Jenna Hakkarainen](https://github.com/jenhakk), [Amanda Karjalainen](https://github.com/amakarj), [Waltteri Grek](https://github.com/GreWalw)

[Link to frontend repository](https://github.com/jenhakk/ICT-Project-Front-end)

![frontend](https://user-images.githubusercontent.com/75020974/207854665-e7a05773-e7c1-446d-9b0d-64f0c1a542de.png) 
 

## Our task


Our task was to explore and try the possibilities of voice recognition in the case of of 112 emergency calls. System should recognize appropriate incident assessments and correct answers from the conversation and it could suggest them to the call taker. Our program shouldn't make actions but only suggest the most likely correct answers to the calltaker.  

Our program consists of four main parts: **backend, frontend, json and Google Speech-to-Text API**. 

The customer's wishes were to concentrate on creating a dynamic backend, creating a json file from which the data will be read and creating an algorithm that supports non-chronological progressing. 

## Introduction 
 
A program which uses ready-made API to recognize speech and transcribe it to text. We have example incident assessments trees made in json, which contain **IDs, values, questions, answers, keywords, negative keywords etc.**  

The program utilizes the algorithm we created to compare transcript words to the keywords of the incident assessment trees, and sends the matching words to the frontend via REST. 

First, the program suggests the right incident assessment tree based on the incident keywords. The calltaker must select the right incident tree, because the program doesn't make the selection of the incident assessment tree by itself. 

After the right incident has been chosen, the program suggests found matches to the user by highlighting the answer options and printing found keywords on the page. 

## Technologies, tools and methods implemented 


* **[Google Cloud Speech-to-text API](https://cloud.google.com/speech-to-text)** 
 
  * API, that transcribes speech either from an audio file or from an audio stream to text 

  * Requires an account to Google Cloud, credits and an API key 

  * We used this because it was familiar to us and we had access to credits 

* **Microphone input as the audio source** 

  * It was the easiest to implement and made possible to use real time audio 

* **Java and REST (backend)**

  * Java is our strongest programming language and there were a lot of examples for our help 

  * REST was chosen because it is familiar to us, it works with React and threading and it enables dataflow to both ways 
 

* **React and JavaScript (frontend)** 

  * React and JavaScript were chosen so we can render the page continuosly without losing data while re-rendering 

* **Github, Github Desktop, Fork** 

  * GitHub was used to have a platform where our code can be shared 

  * These tools were familiar to us 

* **Jira and Confluence for project management** 

  * Jira was used to create and manage sprints and issues 

  * Confluence was used to create and store all the documentation related to the project 

 

## What skills/concepts/information were required to meet our project goals?  

* **Creating and handling a nested JSON** 

  * We didn't have previous knowledge on how to build and handle this kind of complicated JSON structure  

  * We had to compare different JSON libraries and choose the one that fits the best for our usage (we used org.json.simple) 
 
  * Using JSON was client's preferation for the incident assessment data 

* **Backend** 

  * We used various lists to handle the json and convert the data into a form that can be handled 

  * The program runs on threads 

  * Object-oriented programming 

* **REST server using two clients (console program and browser)** 

  * Our program runs as the Java application and App Engine server so we needed a connector between them  

* **Socket for data transfer** 

  * Was used to get data from the frontend to the backend 

* **Google Cloud Speech-to-text API and API key** 

  * We had to learn how the API works and how to implement it on the code 

* **POM dependencies** 

  * Researching right dependencies so we got the necessary libraries in use 


## Conclusions: 

### What did we accomplish?
 
We made a working solution which can suggest the right incident assessment tree to the call taker. After the call taker has confirmed the right incident assesement tree, the program can suggest right answers to the assessments tree's questions. 

The backend is working dynamically and new incident assessments trees can be added to the json-file. 

### How did we meet our goals? 

Goals were met quite well. Time was limited, so we had to prioritize everything pretty carefully, for example the frontend's implementation is not dynamic (on the other hand, this was not a customer's wish either) and the backend doesn't contain all the features we wanted to create. 

Our customer was pleased with the outcome and the product met the set requirements. 

### What challenges we met?

* **JSON**
  * We didn't have previous experience with handling JSON files, so it took us considerable amount of time to figure out the best approach to handle it.
  * We tried using different libraries and lists (e.g. HashMap), but the code ended up being too complex to understand and use.
  * Solution was to use Java objects to handle the data because they helped us to easily access the data from json.

* **Working with two clients and REST**
  * In our previous projects, we hadn't used multiple clients so we didn't know how to transfer data between them and server.
  * We started using ClientBuilder on transfering data from backend to frontend, but it wasn't the most beautiful or functional solution.
  * Then when we tried to get data from frontend to backend, we discovered Sockets and it worked much better. We could have probably used in the first case as well, but didn't have time to change it anymore.


### Development ideas for the future: 

* Recognizing multiple answers from a single transcript line 

* Recognizing different voices (speakers) 

* Recognizing questions also 

* Continuing to listen on possibility on other incidents after the selection of the incident tree has been made 

* The form could be based on the incident assessment json, so it would be dynamic  

* Improving the accuracy of the speech recognition by creating model adaptation  

 
