const express = require('express')
const port = 3000
const fs = require('fs')
const bodyParser = require('body-parser');

const dataPath = './data/registeredFaces.json';

const app = express()
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

app.get('/', (req, res) => {
  res.send('Hello World!')
})

app.get('/faces', (req, res) => {
  fs.readFile(dataPath, 'utf8', (err, data) => {
      if (err) {
        throw err;
      }

      res.send(JSON.parse(data));
    });
})

app.post('/faces', (req, res) => {
  try {
    console.log('Got body:', req);
    fs.writeFileSync(dataPath, JSON.stringify(req.body))
    res.send({success: true, msg: 'File updated	 successfully'})
  } catch (err) {
    console.error(err)
  }
})

const storeData = (data, path) => {
  try {
    fs.writeFileSync(path, JSON.stringify(data))
  } catch (err) {
    console.error(err)
  }
}

const loadData = (path) => {
  try {
    return fs.readFileSync(path, 'utf8')
  } catch (err) {
    console.error(err)
    return false
  }
}


app.listen((process.env.PORT || port), () => {
  console.log(`Example app listening at http://localhost:${port}`)
})
