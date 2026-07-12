import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export default function Dashboard() {
  const [tickets, setTickets] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [view, setView] = useState(""); 
  const [searchQuery, setSearchQuery] = useState("");
  const [error, setError] = useState("");
  const [stats, setStats] = useState(null); 
  
  const navigate = useNavigate();

  useEffect(() => {
    fetchData();

    const socketUrl = window.location.hostname === "localhost" 
      ? "http://localhost:8080/ws-helpdesk" 
      : `${import.meta.env.VITE_API_BASE_URL}/ws-helpdesk`;

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient.subscribe('/topic/tickets', () => {
          fetchData();
        });
      },
    });

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, []);

  const fetchData = async () => {
    try {
      const token = localStorage.getItem("jwt_token");
      if (!token) return navigate("/");

      const headers = { 
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json"
      };

      const [userResponse, ticketsResponse] = await Promise.all([
        axios.get("/api/users/me", { headers }),
        axios.get("/api/tickets", { headers })
      ]);
      
      setCurrentUser(userResponse.data);
      setTickets(ticketsResponse.data);
      
      if (!view) {
        setView(userResponse.data.role === 'ROLE_AGENT' ? 'QUEUE' : 'ACTIVE');
      }

      if (userResponse.data.role === 'ROLE_AGENT') {
        try {
          const statsResponse = await axios.get("/api/tickets/stats", { headers });
          setStats(statsResponse.data);
        } catch (statErr) {
          console.error("Failed to load stats:", statErr);
        }
      }
    } catch (err) {
      setError("Session expired. Please log in again.");
      localStorage.removeItem("jwt_token");
      navigate("/");
    }
  };

  const handleAction = async (ticketId, action) => {
    try {
      const token = localStorage.getItem("jwt_token");
      await axios.put(`/api/tickets/${ticketId}/${action}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
    } catch (err) {
      alert(`Error: ${err.response?.data?.message || "Unknown error"}`);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("jwt_token");
    navigate("/");
  };

  const getPriorityColor = (priority) => {
    switch(priority) {
      case 'CRITICAL': return 'bg-red-100 text-red-800 border-red-200';
      case 'HIGH': return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      default: return 'bg-green-100 text-green-800 border-green-200';
    }
  };

  const getSlaBadge = (createdAt, status) => {
    if (status === 'RESOLVED') return null;
    const hoursOpen = Math.floor((new Date() - new Date(createdAt)) / (1000 * 60 * 60));
    
    if (hoursOpen >= 48) {
      return <span className="px-2 py-1 bg-red-50 text-red-700 border border-red-200 rounded text-[10px] font-bold tracking-wider uppercase">SLA Breached ({hoursOpen}h)</span>;
    }
    if (hoursOpen >= 24) {
      return <span className="px-2 py-1 bg-orange-50 text-orange-700 border border-orange-200 rounded text-[10px] font-bold tracking-wider uppercase">Warning ({hoursOpen}h)</span>;
    }
    return <span className="px-2 py-1 bg-green-50 text-green-700 border border-green-200 rounded text-[10px] font-bold tracking-wider uppercase">On Track ({hoursOpen}h)</span>;
  };

  const getFilteredTickets = () => {
    if (!currentUser) return [];

    let filtered = tickets;
    const isEmployee = ['USER', 'ROLE_USER', 'EMPLOYEE', 'ROLE_EMPLOYEE'].includes(currentUser.role);
    const isAgent = ['AGENT', 'ROLE_AGENT'].includes(currentUser.role);

    if (isEmployee) {
      if (view === "ACTIVE") filtered = filtered.filter(t => t.status !== "RESOLVED");
      if (view === "RESOLVED") filtered = filtered.filter(t => t.status === "RESOLVED");
    }

    if (isAgent) {
      if (view === "QUEUE") filtered = filtered.filter(t => t.status === "OPEN");
      if (view === "WORKSPACE") filtered = filtered.filter(t => t.status === "IN_PROGRESS" && t.assignedTo?.id === currentUser.id);
      if (view === "TEAM") filtered = filtered.filter(t => t.status === "IN_PROGRESS" && t.assignedTo?.id !== currentUser.id);
      if (view === "RESOLVED") filtered = filtered.filter(t => t.status === "RESOLVED");
    }

    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(t => 
        t.title.toLowerCase().includes(query) || 
        t.description.toLowerCase().includes(query) ||
        t.id.toString().includes(query)
      );
    }

    return filtered;
  };

  const filteredTickets = getFilteredTickets().sort((a, b) => 
    new Date(b.createdAt) - new Date(a.createdAt)
  );

  const isAgent = currentUser?.role === 'ROLE_AGENT';

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm border-b border-gray-200 px-6 py-4 flex justify-between items-center">
        <div>
          <h2 className="text-xl font-bold text-gray-800 tracking-tight">Synapse Command Center</h2>
          {currentUser && (
            <p className="text-sm text-gray-500">
              Welcome, {currentUser.name} | <span className="font-semibold text-blue-600">{isAgent ? 'IT Agent' : 'Employee'}</span>
            </p>
          )}
        </div>
        <div className="flex gap-3">
          {/* Add this Analytics Button for Agents */}
          {isAgent && (
            <button onClick={() => navigate("/analytics")} className="px-4 py-2 bg-indigo-50 text-indigo-700 text-sm font-medium rounded-lg hover:bg-indigo-100 border border-indigo-200 transition-colors shadow-sm hidden sm:block">
              View Analytics
            </button>
          )}
          
          <button onClick={() => navigate("/create-ticket")} className="px-4 py-2 bg-emerald-600 text-white text-sm font-medium rounded-lg hover:bg-emerald-700 transition-colors shadow-sm">
            + Create Ticket
          </button>
          <button onClick={handleLogout} className="px-4 py-2 bg-white text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-50 border border-gray-300 transition-colors shadow-sm">
            Logout
          </button>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto py-8 px-6">
        
        {error && (
          <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 text-red-700 rounded-r-lg">
            {error}
          </div>
        )}

        {isAgent && stats && (
          <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-200">
              <p className="text-sm text-gray-500 font-medium mb-1">Total Tickets</p>
              <h4 className="text-2xl font-bold text-gray-900">{stats.totalTickets}</h4>
            </div>
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-200">
              <p className="text-sm text-blue-500 font-medium mb-1">Open</p>
              <h4 className="text-2xl font-bold text-blue-700">{stats.openTickets}</h4>
            </div>
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-200">
              <p className="text-sm text-yellow-600 font-medium mb-1">In Progress</p>
              <h4 className="text-2xl font-bold text-yellow-700">{stats.inProgressTickets}</h4>
            </div>
            <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-200">
              <p className="text-sm text-green-600 font-medium mb-1">Resolved</p>
              <h4 className="text-2xl font-bold text-green-700">{stats.resolvedTickets}</h4>
            </div>
            <div className="bg-white p-4 rounded-xl shadow-sm border border-red-200 bg-red-50">
              <p className="text-sm text-red-600 font-medium mb-1">Critical</p>
              <h4 className="text-2xl font-bold text-red-700">{stats.criticalTickets}</h4>
            </div>
          </div>
        )}

        <div className="flex flex-col md:flex-row gap-4 justify-between items-start md:items-center mb-8 pb-4 border-b border-gray-200">
          <div className="flex gap-2 overflow-x-auto w-full md:w-auto">
            {isAgent ? (
              <>
                <button onClick={() => setView("QUEUE")} className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${view === "QUEUE" ? "bg-blue-100 text-blue-800" : "text-gray-600 hover:bg-gray-100"}`}>Queue (Open)</button>
                <button onClick={() => setView("WORKSPACE")} className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${view === "WORKSPACE" ? "bg-blue-100 text-blue-800" : "text-gray-600 hover:bg-gray-100"}`}>My Workspace</button>
                <button onClick={() => setView("TEAM")} className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${view === "TEAM" ? "bg-blue-100 text-blue-800" : "text-gray-600 hover:bg-gray-100"}`}>Team Load</button>
                <button onClick={() => setView("RESOLVED")} className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${view === "RESOLVED" ? "bg-blue-100 text-blue-800" : "text-gray-600 hover:bg-gray-100"}`}>Resolved</button>
              </>
            ) : (
              <>
                <button onClick={() => setView("ACTIVE")} className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${view === "ACTIVE" ? "bg-blue-100 text-blue-800" : "text-gray-600 hover:bg-gray-100"}`}>Active Tickets</button>
                <button onClick={() => setView("RESOLVED")} className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors whitespace-nowrap ${view === "RESOLVED" ? "bg-blue-100 text-blue-800" : "text-gray-600 hover:bg-gray-100"}`}>History</button>
              </>
            )}
          </div>

          <div className="w-full md:w-64 relative">
            <svg className="w-4 h-4 absolute left-3 top-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
            <input 
              type="text" 
              placeholder="Search tickets..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-sm border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 focus:outline-none transition-shadow"
            />
          </div>
        </div>

        {filteredTickets.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-xl border border-dashed border-gray-300">
            <p className="text-gray-500 font-medium">No tickets found matching your criteria.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredTickets.map(ticket => (
              <div 
                key={ticket.id} 
                onClick={() => navigate(`/ticket/${ticket.id}`)}
                className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex flex-col hover:shadow-md hover:border-blue-300 transition-all cursor-pointer relative"
              >
                
                <div className="flex justify-between items-start mb-3">
                  <h3 className="text-lg font-bold text-gray-900 leading-tight flex-1 mr-3">
                    #{ticket.id} {ticket.title}
                  </h3>
                  <div className="flex flex-col items-end gap-2">
                    <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border ${getPriorityColor(ticket.priority)}`}>
                      {ticket.priority}
                    </span>
                    {getSlaBadge(ticket.createdAt, ticket.status)}
                  </div>
                </div>
                
                <p className="text-gray-600 text-sm mb-4 line-clamp-3 flex-grow">
                  {ticket.description}
                </p>

                <div className="mt-auto pt-4 border-t border-gray-100 text-xs text-gray-500 space-y-1">
                  <p>Created: {new Date(ticket.createdAt).toLocaleDateString()} by {ticket.createdBy?.name}</p>
                  {ticket.assignedTo && <p>Assigned to: <span className="font-semibold">{ticket.assignedTo.name}</span></p>}
                </div>
                
                {isAgent && ticket.status === 'OPEN' && (
                  <button 
                    onClick={(e) => { 
                      e.stopPropagation(); 
                      handleAction(ticket.id, "assign"); 
                    }} 
                    className="mt-4 w-full py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg font-medium hover:bg-blue-100 transition-colors"
                  >
                    Claim Ticket
                  </button>
                )}

                {isAgent && ticket.status === 'IN_PROGRESS' && ticket.assignedTo?.id === currentUser.id && (
                  <button 
                    onClick={(e) => { 
                      e.stopPropagation(); 
                      handleAction(ticket.id, "resolve"); 
                    }} 
                    className="mt-4 w-full py-2 bg-green-50 text-green-700 border border-green-200 rounded-lg font-medium hover:bg-green-100 transition-colors"
                  >
                    Mark Resolved
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}