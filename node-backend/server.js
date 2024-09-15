const express = require('express');
const cors = require('cors');
const app = express();
const bodyParser = require('body-parser');


app.use(cors({ origin: 'http://localhost:4200' }));
app.use(express.json()); // Use JSON request bodies
app.use(bodyParser.urlencoded({extended: true}) );


 // Listen to the App Engine-specified port, or 8080 otherwise
 const PORT = process.env.PORT || 8080;
 app.listen(PORT, () => {
   console.log(`Server listening on port ${PORT}...`);
 });


// now we get data from FinHub API
FINNHUB_API_KEY = 'cmsaabhr01qlk9b15o5gcmsaabhr01qlk9b15o60'
FINNHUB_BASE_URL_1 = 'https://finnhub.io/api/v1/stock/profile2'
FINNHUB_BASE_URL_2 = 'https://finnhub.io/api/v1/quote'
FINNHUB_BASE_URL_3 = 'https://finnhub.io/api/v1/search'
FINNHUB_BASE_URL_4 = 'https://finnhub.io/api/v1/stock/recommendation'
FINNHUB_BASE_URL_5 = 'https://finnhub.io/api/v1/stock/earnings'
FINNHUB_BASE_URL_6 = 'https://finnhub.io/api/v1/stock/peers'
FINNHUB_BASE_URL_7 = 'https://finnhub.io/api/v1/company-news'
FINNHUB_BASE_URL_8 = 'https://finnhub.io/api/v1/stock/insider-sentiment'

POLYGON_API_KEY = 'H1yqa2g6YarOCvTqnQULhJS7WLPGSp_M'
POLYGON_BASE_URL = 'https://api.polygon.io/v2/aggs'

const axios = require('axios');
const moment = require('moment');




//getting Company's Description 
app.get('/search-company/:tickerSymbol', (req, res) => {
    const tickerSymbol = req.params.tickerSymbol;

    axios.get(`${FINNHUB_BASE_URL_1}?symbol=${tickerSymbol}&token=${FINNHUB_API_KEY}`)
    .then(response => {
      res.json(response.data);
    })
    .catch(error => {
      console.error("Error fetching company data:", error);
      res.status(500).json({ error: "An error occurred while fetching data from FinHub API." });
    });
});

app.get('/search-autocomplete/:tickerSymbol', (req, res) => {
  const tickerSymbol = req.params.tickerSymbol;

  axios.get(`${FINNHUB_BASE_URL_3}?q=${tickerSymbol}&token=${FINNHUB_API_KEY}`)
  .then(response => {
    res.json(response.data);
  })
  .catch(error => {
    console.error("Error fetching autocomplete data:", error);
    res.status(500).json({ error: "An error occurred while fetching data from FinHub API." });
  });
})



//getting Company's Stock Quote
app.get('/search-quote/:tickerSymbol', async (req, res) => {
  try {
    const tickerSymbol = req.params.tickerSymbol;

    const response = await axios.get(`${FINNHUB_BASE_URL_2}?symbol=${tickerSymbol}&token=${FINNHUB_API_KEY}`);
    res.json(response.data);
    
  } catch (error) {
      console.error("Error fetching quote data:", error);
      res.status(500).json({ error: "An error occurred while fetching data from FinHub API." });
  }

});

//getting company's peers
app.get('/search-peers/:tickerSymbol', async(req, res) => {
  try {
    const tickerSymbol = req.params.tickerSymbol;

    const response = await axios.get(`${FINNHUB_BASE_URL_6}?symbol=${tickerSymbol}&token=${FINNHUB_API_KEY}`);
    res.json(response.data);
    
  } catch (error) {
      console.error("Error fetching company peers data:", error);
      res.status(500).json({ error: "An error occurred while fetching data from FinHub API." });
  }
});

//getting company's aggregate (hourly price)
app.get('/search-hourly-price/:tickerSymbol/:fromDate/:toDate', async (req, res)=>{

  try{
    const tickerSymbol = req.params.tickerSymbol;
    const fromDate = req.params.fromDate;
    const toDate = req.params.toDate;
    const apiUrl = `${POLYGON_BASE_URL}/ticker/${tickerSymbol}/range/1/hour/${fromDate}/${toDate}?adjusted=true&sort=asc&apiKey=${POLYGON_API_KEY}`;
    //console.log('hourly price url is:',apiUrl);
    const response = await axios.get(apiUrl);
    res.json(response.data);
  } catch(error){
    console.log("An error occured while fetching hourly data", error);
    res.status(500).json({ error: "An error occurred while fetching data from Polygon" });
  }
});

//getting the top news
app.get('/search-news/:tickerSymbol', async(req, res) => {

  try {
    const tickerSymbol = req.params.tickerSymbol;

    //calculating today's date and one week before in YYYY-MM-DD format
    const toDate = new Date().toISOString().split('T')[0]; 
    const fromDate = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]; 
    //console.log('toDate is;', toDate);
    //console.log('fromDate is:', fromDate);
    const apiUrl = `${FINNHUB_BASE_URL_7}?symbol=${tickerSymbol}&from=${fromDate}&to=${toDate}&token=${FINNHUB_API_KEY}`;

    const response = await axios.get(apiUrl);
    res.json(response.data);
    
  } catch (error) {
      console.error("Error fetching company top news data:", error);
      res.status(500).json({ error: "An error occurred while fetching data from FinHub API." });
  }

});

//getting the chart tab data
app.get('/search-chart/:tickerSymbol',async (req, res) =>{
  const tickerSymbol = req.params.tickerSymbol;

  const toDate = new Date();
  toDate.setDate(toDate.getDate() - 1);
  //console.log('toDate is:',toDate);
  const toDateFormatted = toDate.getTime();
  

  const currentDate = new Date();
  const fromDate = new Date(currentDate.getFullYear() - 2, currentDate.getMonth(), currentDate.getDate() - 1);
  //console.log('fromDate is:',fromDate);
  const fromDateFormatted = fromDate.getTime();


  const apiUrl = `${POLYGON_BASE_URL}/ticker/${tickerSymbol}/range/1/day/${fromDateFormatted}/${toDateFormatted}?adjusted=true&sort=asc&apiKey=${POLYGON_API_KEY}`;
  //console.log(apiUrl);

  axios.get(apiUrl)
        .then(response => {
            res.json(response.data);
        })
        .catch(error => {
            console.error("Error fetching data:", error);
            res.status(500).json({ error: "An error occurred while fetching data from Polygon.io API." });
        });
  });

//getting insider sentiments
app.get('/search-sentiments/:tickerSymbol', async(req, res) => {
  const tickerSymbol = req.params.tickerSymbol;

  try {
    const response = await axios.get(`${FINNHUB_BASE_URL_8}?symbol=${tickerSymbol}&from=2022-01-01&token=${FINNHUB_API_KEY}`);
    res.json(response.data);
  }
  catch(error) {
    console.error("Error fetching insider sentiments data:", error);
    res.status(500).json({ error: "An error occurred while fetching data from FinHub API." });
  }

});


//getting the recommendation trends data
app.get('/search-recommendations/:tickerSymbol', async(req, res) => {
  const tickerSymbol = req.params.tickerSymbol;

  try{
    const response = await axios.get(`${FINNHUB_BASE_URL_4}?symbol=${tickerSymbol}&token=${FINNHUB_API_KEY}`);
    res.json(response.data);
  } catch(error){
      console.error("Error fetching recommendation data:", error);
      res.status(500).json({ error: "An error occurred while fetching data from FinHub API." });
  }

});

//getting earnings data
app.get('/search-earnings/:tickerSymbol', async(req, res)=>{
  const tickerSymbol = req.params.tickerSymbol;
  try{
    const response = await axios.get(`${FINNHUB_BASE_URL_5}?symbol=${tickerSymbol}&token=${FINNHUB_API_KEY}`);
    res.json(response.data);
  }catch(error){
    console.error("Error fetching earnings data:", error);
    res.status(500).json({ error: "An error occurred while fetching data from FinHub API." });
  }
})

//setting up MongoDB

const { MongoClient } = require('mongodb');


const CONNECTION_STRING = `mongodb+srv://negar6868:${encodeURIComponent("8uzkNFYgSYa#!w9")}@cluster0.dq6yhnn.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0`;
const databaseName = 'HW3';
const COLLECTION_NAME = 'HW3.favorites';

MongoClient.connect(CONNECTION_STRING)
  .then(client => {
    const db = client.db('HW3'); 
    const collection = db.collection('favorites'); 
    console.log('Connected to MongoDB HW3.favorites');

    // route get the favorite stocks
    app.get('/api/favorites/getFavorites', async (req, res) => {
      try {
        const data = await collection.find({}).toArray();
        res.json(data);
      } catch (error) {
        console.error('Error fetching data:', error);
        res.status(500).json({ error: 'An error occurred while fetching data from MongoDB.' });
      }
    });
    
    //route to add new favorite stocks
    app.post('/api/favorites/addFavorites', async (req, res) => {
      try {
        const { tickerSymbol, companyName } = req.body; 
    
        await collection.insertOne({ tickerSymbol, companyName });
    
        res.status(201).json({ message: 'Favorite stock added successfully!' });
      } catch (error) {
        console.error('Error adding favorite stock:', error);
        res.status(500).json({ error: 'An error occurred while adding favorite stock.' });
      }
    });

    //route to delete a previosuly added favorite stock
    app.delete('/api/favorites/deleteFavorites/:tickerSymbol', async (req, res) => {
      try {
        const tickerSymbol = req.params.tickerSymbol;
        const result = await collection.deleteOne({ tickerSymbol });
        if (result.deletedCount == 0) {
          res.status(404).json({ error: 'Favorite stock not found.' });
        } else {
          res.json({ message: 'Favorite stock deleted successfully!' });
        }
      } catch (error) {
        console.error('Error deleting favorite stock:', error);
        res.status(500).json({ error: 'An error occurred while deleting favorite stock.' });
      }
    });
  })
  .catch(error => {
    console.error('Error connecting to MongoDB HW3.favorites:', error);
  });

MongoClient.connect(CONNECTION_STRING)
  .then(client => {
    const db = client.db('HW3'); 
    const collection = db.collection('wallet'); 
    console.log('Connected to MongoDB HW3.wallet');

    // route get the wallet cash balance
    app.get('/api/wallet/getBalance', async (req, res) => {
      try {
        const balanceData = await collection.find({}).toArray();
        //console.log('Balance data:', balanceData);
        res.json(balanceData);
      } catch (error) {
        console.error('Error fetching data:', error);
        res.status(500).json({ error: 'An error occurred while fetching data from MongoDB.' });
      }
    });
    
    //route to update the cash balance in wallet
    app.put('/api/wallet/updateBalance/', async (req, res) => {
      try {

        const { cash_balance } = req.body; 
        const result = await collection.updateOne(
          { }, //we update all the fields in the collection
          { $set: { cash_balance: cash_balance }}
        );
    
        if (result.modifiedCount == 0) {
          res.status(404).json({ error: 'Balance not Updated .' });
        } else {
          res.json({ message: 'Balance updated successfully!' });
        }
      } catch (error) {
        console.error('Error updating wallet balance:', error);
        res.status(500).json({ error: 'An error occurred while updating wallet balance.' });
      }
    });
  })
  .catch(error => {
    console.error('Error connecting to MongoDB HW3.wallet:', error);
  });

MongoClient.connect(CONNECTION_STRING)
  .then(client => {
    const db = client.db('HW3'); 
    const collection = db.collection('portfolio'); 
    console.log('Connected to MongoDB HW3.portfolio');

    // route get the portflio data
    app.get('/api/portfolio/getPortfolio', async (req, res) => {
      try {
        const portfolioData = await collection.find({}).toArray();
        res.json(portfolioData);
      } catch (error) {
        console.error('Error fetching data:', error);
        res.status(500).json({ error: 'An error occurred while fetching data from MongoDB.' });
      }
    });

    //route to add a portfolio item
    app.post('/api/portfolio/addPortfolio', async (req, res) => {
      try {
       
        const { ticker, company_name, quantity, average_cost_share, total_cost, change, current_price, market_value } = req.body; 
    
        await collection.insertOne({ticker, company_name, quantity, average_cost_share, total_cost, change, current_price, market_value});
        res.json({ message: 'Portfolio item added successfully!' });
      } catch (error) {
        console.error('Error adding portfolio item', error);
        res.status(500).json({ error: 'An error occurred while adding favorite stock.' });
      }
    });

    //route to delete an existing portfolio item
    app.delete('/api/portfolio/deletePortfolio/:tickerSymbol', async (req, res) => {
      try {
        const tickerToDelete = req.params.tickerSymbol;
        const result = await collection.deleteOne({ ticker: tickerToDelete });
        if (result.deletedCount == 0) {
          res.status(404).json({ error: 'Portfolio item not found.' });
        } else {
          res.json({ message: 'Portfolio item deleted successfully!' });
        }
      } catch (error) {
        console.error('Error deleting portfolio item:', error);
        res.status(500).json({ error: 'An error occurred while deleting portfolio.' });
      }
    });

    app.put('/api/portfolio/updatePortfolio/:tickerSymbol', async(req, res) => {
      const ticker = req.params.tickerSymbol;
      const newData = req.body; 
    
      try {
        const existingPortfolioItem = await collection.findOne({ ticker: ticker });
        if (existingPortfolioItem) {
          // If the ticker exists, update the existing entry
          await collection.updateOne({ ticker: ticker }, { $set: newData });
          res.status(200).json({ message: `Data for ticker ${ticker} updated successfully.` });
        } else {
          // If the ticker doesn't exist, add a new entry
          await collection.insertOne({ ticker, ...newData });
          res.status(200).json({message: `New data added for ticker ${ticker}.`});
        }
      } catch (error) {
        console.error('Error updating or adding portfolio item:', error);
        res.status(500).json({ error: 'An error occurred while updating or adding portfolio item.' });
      }
    });
    

  })
  .catch(error => {
    console.error('Error connecting to MongoDB HW3.wallet:', error);
  });