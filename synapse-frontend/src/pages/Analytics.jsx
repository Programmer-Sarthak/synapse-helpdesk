import { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";

export default function Analytics() {
  const [data, setData] = useState(null);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const token = localStorage.getItem("jwt_token");
      if (!token) return navigate("/");
      
      const response = await axios.get("/api/analytics", {
        headers: { Authorization: `Bearer ${token}` }
      });
      setData(response.data);
    } catch (err) {
      if (err.response?.status === 403) {
        navigate("/dashboard"); 
      } else {
        setError("Failed to load analytics");
      }
    }
  };

  const exportToCSV = () => {
    if (!data) return;

    // Create CSV Headers
    let csvContent = "data:text/csv;charset=utf-8,";
    csvContent += "Agent Name,Total Tickets Resolved\n";

    // Add Leaderboard Data
    Object.entries(data.agentLeaderboard)
      .sort(([,a], [,b]) => b - a)
      .forEach(([name, count]) => {
        csvContent += `"${name}",${count}\n`;
      });

    // Create a downloadable link and trigger it
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `agent_performance_report_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (!data) return <div className="p-8 text-center text-gray-500 font-medium">Crunching the numbers...</div>;

  const trendData = Object.keys(data.ticketsByDay).map(key => ({
    name: key,
    tickets: data.ticketsByDay[key]
  }));

  const priorityData = Object.keys(data.ticketsByPriority).map(key => ({
    name: key,
    value: data.ticketsByPriority[key]
  }));

  const COLORS = ['#10B981', '#F59E0B', '#EF4444', '#3B82F6'];

  return (
    <div className="min-h-screen bg-gray-50 pb-12">
      <nav className="bg-white shadow-sm border-b border-gray-200 px-6 py-4 flex justify-between items-center sticky top-0 z-10">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate("/dashboard")} className="text-gray-500 hover:text-gray-800 font-medium transition-colors">
            &larr; Back to Command Center
          </button>
          <h2 className="text-xl font-bold text-gray-800 tracking-tight">System Analytics</h2>
        </div>
        <button 
          onClick={exportToCSV}
          className="px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-lg hover:bg-green-700 transition-colors shadow-sm flex items-center gap-2"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
          Export CSV Report
        </button>
      </nav>

      <main className="max-w-7xl mx-auto mt-8 px-6 space-y-6">
        
        {error && <div className="p-4 bg-red-50 text-red-700 rounded-lg border border-red-200">{error}</div>}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <h3 className="text-lg font-bold text-gray-900 mb-6">7-Day Ticket Volume</h3>
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={trendData}>
                  <XAxis dataKey="name" stroke="#9CA3AF" fontSize={12} tickLine={false} axisLine={false} />
                  <YAxis stroke="#9CA3AF" fontSize={12} tickLine={false} axisLine={false} allowDecimals={false} />
                  <Tooltip cursor={{fill: '#F3F4F6'}} contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
                  <Bar dataKey="tickets" fill="#3B82F6" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
            <h3 className="text-lg font-bold text-gray-900 mb-6">Priority Distribution</h3>
            <div className="h-64 flex justify-center">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={priorityData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={80}
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {priorityData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)'}} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="flex justify-center gap-4 mt-4">
              {priorityData.map((entry, index) => (
                <div key={entry.name} className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }}></div>
                  <span className="text-sm text-gray-600">{entry.name}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200 lg:col-span-2">
            <h3 className="text-lg font-bold text-gray-900 mb-6">Agent Leaderboard (Resolved Tickets)</h3>
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-gray-100">
                    <th className="py-3 px-4 text-sm font-semibold text-gray-500 uppercase tracking-wider">Agent Name</th>
                    <th className="py-3 px-4 text-sm font-semibold text-gray-500 uppercase tracking-wider text-right">Total Resolved</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-50">
                  {Object.entries(data.agentLeaderboard)
                    .sort(([,a], [,b]) => b - a)
                    .map(([name, count]) => (
                    <tr key={name} className="hover:bg-gray-50 transition-colors">
                      <td className="py-3 px-4 text-sm font-medium text-gray-900">{name}</td>
                      <td className="py-3 px-4 text-sm text-gray-600 text-right font-semibold">{count}</td>
                    </tr>
                  ))}
                  {Object.keys(data.agentLeaderboard).length === 0 && (
                    <tr>
                      <td colSpan="2" className="py-8 text-center text-gray-500 text-sm">No tickets have been resolved yet.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

        </div>
      </main>
    </div>
  );
}