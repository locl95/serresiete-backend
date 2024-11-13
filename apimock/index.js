const express = require('express')
const {join} = require("path");
const app = express()
const port = 8080
const cors = require('cors')

app.use(express.json());
app.use(cors())

app.get('/api/views/:id/data', (req, res) => {
    res.sendFile(join(__dirname, 'resources', 'data.json'));
});

app.get('/api/views/:id/cached-data', (req, res) => {
    res.sendFile(join(__dirname, 'resources', 'cached-data.json'));
});

app.get('/api/views', (req, res) => {
    res.sendFile(join(__dirname, 'resources', 'views.json'));
});

app.listen(port, () => {
    console.log(`Mock server running at http://localhost:${port}`);
});
