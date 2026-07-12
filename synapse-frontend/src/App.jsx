import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import CreateTicket from "./pages/CreateTicket";
import TicketDetails from "./pages/TicketDetails"; // 🚀 Add this import
import axios from "axios";
import Analytics from "./pages/Analytics"; // Add this import

const getBaseUrl = () => {
  const localUrl = "http://localhost:8080";
  const cloudUrl = import.meta.env.VITE_API_BASE_URL;
  return window.location.hostname === "localhost" ? localUrl : cloudUrl;
};

axios.defaults.baseURL = getBaseUrl();

function App() {
  return (
      <Router>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/create-ticket" element={<CreateTicket />} />
          <Route path="/ticket/:id" element={<TicketDetails />} /> {/* 🚀 Add this route */}
          <Route path="/register" element={<Register />} />
          <Route path="/analytics" element={<Analytics />} />
        </Routes>
      </Router>
  );
}

export default App;