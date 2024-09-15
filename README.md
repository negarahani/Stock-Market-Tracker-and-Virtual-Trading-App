# Stock Market Tracker and Virtual Trading App

This Android application allows users to search for stock symbols/tickers, view detailed stock information, trade with virtual money, and manage a portfolio. Users can also track their favorite stock symbols. The app has two main screens: the Home screen and the Detailed Stock Information screen, each with multiple features.

## Key Features

1. **Home Screen:**
   - **Portfolio Section:** Displays cash balance, net worth, and a list of owned stocks with relevant details such as stock symbol, market value, price change, and total shares owned. Users start with a virtual balance of $25,000.
   - **Favorites Section:** Lists user-favorited stocks with current prices, changes, and percentage changes. Stocks are displayed with color-coded symbols indicating price trends.
   - **Swipe to Delete:** Allows users to remove stocks from the favorites section, updating both the view and the backend database.
   - **Drag and Reorder:** Users can reorder stocks within each section using drag-and-drop, with safeguards to prevent cross-section moves.

2. **Search Functionality:**
   - Integrated search bar with autocomplete suggestions for stock symbols using the Finnhub Autocomplete API. Selecting a suggestion redirects to the Detailed Stock Information screen.

3. **Detailed Stock Information Screen:**
   - Displays comprehensive information about selected stocks, including company details, real-time stock prices, historical charts, and news articles.
   - Users can add or remove stocks from their favorites, with corresponding UI changes and toast notifications.
   - **Trading Functionality:** Allows users to buy or sell shares, with a trade dialog showing the current balance, calculated trade values, and error handling for invalid trades.

4. **Additional Features:**
   - **App Icon and Splash Screen:** The app starts with a splash screen displaying the app icon.
   - **Progress Spinner:** Shown during data fetching.
   - **Dynamic Price Updates:** Stock prices update every 15 seconds on the home screen.

## Technical Details

- The app utilizes 9 API calls to Finnhub for data such as company profiles, stock quotes, news, recommendations, and chart data.
- Backend services are implemented using **Node.js** and **Express**, providing robust API endpoints for data handling.
- Implemented using RecyclerView and ConstraintLayout for a clean and responsive UI.
- Search and detail views are designed to enhance user experience with seamless navigation and real-time data display.

## Technologies Used

- **Java:** To develop the Android application, including implementing core functionality and handling Android Lifecycle events.
- **JSON:** For parsing and handling data received from the Finnhub APIs.
- **Android Studio:** The primary Integrated Development Environment (IDE) used for developing and testing the Android app.
- **Google Material Design:** Applied to ensure the app adheres to modern design principles and provides an aesthetically pleasing user experience.
- **Finnhub APIs:** Utilized for fetching stock market data, including company profiles, stock quotes, and news.
- **Android SDK:** Provides the necessary tools and libraries for Android app development.
- **Picasso & Glide:** Libraries used for efficient image loading and display, handling various image-related tasks.
- **Volley:** Employed for network operations, including making API calls and handling responses.
- **Node.js & Express:** Backend technologies used to create and manage API endpoints and handle server-side logic.
  
## Demo Video

[Watch the demo video](https://www.youtube.com/watch?v=154yEzqdS1I)
