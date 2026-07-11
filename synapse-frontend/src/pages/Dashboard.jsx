import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

export default function Dashboard() {
  const [tickets, setTickets] = useState([]);
  const [currentUser, setCurrentUser] = useState(null);
  const [view, setView] = useState(""); 
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      const token = localStorage.getItem("jwt_token");
      if (!token) return navigate("/");

      const headers = { Authorization: `Bearer ${token}` };

      const [userResponse, ticketsResponse] = await Promise.all([
        axios.get("/api/users/me", { headers }),
        axios.get("/api/tickets", { headers })
      ]);
      
      setCurrentUser(userResponse.data);
      setTickets(ticketsResponse.data);


      
      // Set the default tab based on their role
      if (!view) {
        setView(userResponse.data.role === 'ROLE_AGENT' ? 'QUEUE' : 'ACTIVE');
      }
    } catch (err) {
      setError("Session expired. Please log in again.");
      navigate("/");
    }
  };

  const handleAction = async (ticketId, action) => {
    try {
      const token = localStorage.getItem("jwt_token");
      await axios.put(`/api/tickets/${ticketId}/${action}`, {}, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchData(); // Refresh the data to move the ticket to the new tab
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

  // 🚀 THE CORE BUSINESS LOGIC: Filter tickets based on Role AND Tab
  const getFilteredTickets = () => {
    if (!currentUser) return [];

    if (currentUser.role === 'ROLE_USER') {
      if (view === "ACTIVE") return tickets.filter(t => t.status !== "RESOLVED");
      if (view === "RESOLVED") return tickets.filter(t => t.status === "RESOLVED");
    }

    if (currentUser.role === 'ROLE_AGENT') {
      if (view === "QUEUE") return tickets.filter(t => t.status === "OPEN");
      if (view === "WORKSPACE") return tickets.filter(t => t.status === "IN_PROGRESS" && t.assignedTo?.id === currentUser.id);
      if (view === "TEAM") return tickets.filter(t => t.status === "IN_PROGRESS" && t.assignedTo?.id !== currentUser.id);
      if (view === "RESOLVED") return tickets.filter(t => t.status === "RESOLVED");
    }
    return [];
  };

  const filteredTickets = getFilteredTickets();

  return (
    <div className="min-h-screen bg-gray-50">
      
      <nav className="bg-white shadow-sm border-b border-gray-200 px-6 py-4 flex justify-between items-center">
        <div>
          <h2 className="text-xl font-bold text-gray-800 tracking-tight">Synapse Command Center</h2>
          {currentUser && (
            <p className="text-sm text-gray-500">
              Welcome, {currentUser.name} | <span className="font-semibold text-blue-600">{currentUser.role === 'ROLE_AGENT' ? 'IT Agent' : 'Employee'}</span>
            </p>
          )}
        </div>
        <div className="flex gap-3">
          <button onClick={() => navigate("/create-ticket")} className="px-4 py-2 bg-emerald-600 text-white text-sm font-medium rounded-lg hover:bg-emerald-700">
            + Create Ticket
          </button>
          <button onClick={handleLogout} className="px-4 py-2 bg-gray-100 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-200 border border-gray-200">
            Logout
          </button>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto py-8 px-6">
        
        {/* 🚀 DYNAMIC TABS BASED ON ROLE */}
        <div className="flex gap-2 mb-6 border-b border-gray-200 pb-4 overflow-x-auto">
          {currentUser?.role === 'ROLE_USER' && (
            <>
              <button onClick={() => setView("ACTIVE")} className={`px-4 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition-colors ${view === "ACTIVE" ? "bg-blue-100 text-blue-700" : "text-gray-500 hover:bg-gray-100"}`}>My Active Tickets</button>
              <button onClick={() => setView("RESOLVED")} className={`px-4 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition-colors ${view === "RESOLVED" ? "bg-gray-200 text-gray-800" : "text-gray-500 hover:bg-gray-100"}`}>Resolution History</button>
            </>
          )}

          {currentUser?.role === 'ROLE_AGENT' && (
            <>
              <button onClick={() => setView("QUEUE")} className={`px-4 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition-colors ${view === "QUEUE" ? "bg-blue-100 text-blue-700" : "text-gray-500 hover:bg-gray-100"}`}>Global Queue (Open)</button>
              <button onClick={() => setView("WORKSPACE")} className={`px-4 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition-colors ${view === "WORKSPACE" ? "bg-emerald-100 text-emerald-800" : "text-gray-500 hover:bg-gray-100"}`}>My Workspace</button>
              <button onClick={() => setView("TEAM")} className={`px-4 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition-colors ${view === "TEAM" ? "bg-purple-100 text-purple-700" : "text-gray-500 hover:bg-gray-100"}`}>Team Activity</button>
              <button onClick={() => setView("RESOLVED")} className={`px-4 py-2 text-sm font-medium rounded-lg whitespace-nowrap transition-colors ${view === "RESOLVED" ? "bg-gray-200 text-gray-800" : "text-gray-500 hover:bg-gray-100"}`}>History</button>
            </>
          )}
        </div>

        {error && <div className="bg-red-50 text-red-700 p-4 mb-6 rounded">{error}</div>}

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredTickets.length === 0 ? (
            <div className="col-span-full text-center py-12 text-gray-500 bg-white rounded-lg border border-dashed border-gray-300">
              No tickets found in this view!
            </div>
          ) : (
            filteredTickets.map(ticket => (
              <div key={ticket.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 flex flex-col">
                
                <div className="flex justify-between items-start mb-4">
                  <h3 className="text-lg font-semibold text-gray-900 leading-tight">#{ticket.id} - {ticket.title}</h3>
                  <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${getPriorityColor(ticket.priority)}`}>
                    {ticket.priority}
                  </span>
                </div>
                
                <p className="text-gray-600 text-sm mb-6 flex-grow">{ticket.description}</p>
                
                <div className="border-t border-gray-100 pt-4 mt-auto space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Status:</span>
                    <span className="font-medium text-gray-900">{ticket.status}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Created By:</span>
                    <span className="font-medium text-gray-900">{ticket.createdBy.name}</span>
                  </div>
                  {ticket.assignedTo && (
                    <div className="flex justify-between">
                      <span className="text-gray-500">Agent:</span>
                      <span className="font-medium text-blue-600">{ticket.assignedTo.name}</span>
                    </div>
                  )}
                </div>

                {/* 🚀 BUTTON SECURITY: Agents can only claim OPEN tickets */}
                {currentUser?.role === 'ROLE_AGENT' && ticket.status === "OPEN" && (
                  <button onClick={() => handleAction(ticket.id, "assign")} className="mt-5 w-full py-2 bg-blue-50 text-blue-700 border border-blue-200 rounded-lg hover:bg-blue-600 hover:text-white transition-colors text-sm font-medium">
                    Claim Ticket
                  </button>
                )}
                
                {/* 🚀 BUTTON SECURITY: Agents can only resolve tickets ASSIGNED TO THEM */}
                {currentUser?.role === 'ROLE_AGENT' && ticket.status === "IN_PROGRESS" && ticket.assignedTo?.id === currentUser.id && (
                  <button onClick={() => handleAction(ticket.id, "resolve")} className="mt-5 w-full py-2 bg-emerald-50 text-emerald-700 border border-emerald-200 rounded-lg hover:bg-emerald-600 hover:text-white transition-colors text-sm font-medium">
                    Mark as Resolved
                  </button>
                )}
              </div>
            ))
          )}
        </div>
      </main>
    </div>
  );
}