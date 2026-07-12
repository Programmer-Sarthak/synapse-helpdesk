import { useState, useEffect, useRef } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export default function TicketDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [ticket, setTicket] = useState(null);
  const [comments, setComments] = useState([]);
  const [attachments, setAttachments] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [newComment, setNewComment] = useState("");
  const [currentUser, setCurrentUser] = useState(null);
  const [isDrafting, setIsDrafting] = useState(false);
  const [aiDisabled, setAiDisabled] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    fetchData();

    const socketUrl = window.location.hostname === "localhost" 
      ? "http://localhost:8080/ws-helpdesk" 
      : `${import.meta.env.VITE_API_BASE_URL}/ws-helpdesk`;

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient.subscribe(`/topic/tickets/${id}`, () => {
          fetchComments(); 
          fetchAuditLogs();
        });
      },
    });

    stompClient.activate();

    return () => stompClient.deactivate();
  }, [id]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [comments]);

  const fetchData = async () => {
    try {
      const token = localStorage.getItem("jwt_token");
      if (!token) return navigate("/");
      const config = { headers: { Authorization: `Bearer ${token}` } };

      const [userRes, commentsRes, attachmentsRes, auditRes, ticketsRes] = await Promise.all([
        axios.get("/api/users/me", config),
        axios.get(`/api/tickets/${id}/comments`, config),
        axios.get(`/api/tickets/${id}/attachments`, config),
        axios.get(`/api/tickets/${id}/audit`, config),
        axios.get("/api/tickets", config)
      ]);
      
      setCurrentUser(userRes.data);
      setComments(commentsRes.data);
      setAttachments(attachmentsRes.data);
      setAuditLogs(auditRes.data);

      const foundTicket = ticketsRes.data.find(t => t.id === parseInt(id));
      if(foundTicket) setTicket(foundTicket);

    } catch (err) {
      console.error(err);
      navigate("/dashboard");
    }
  };

  const fetchComments = async () => {
    const token = localStorage.getItem("jwt_token");
    const res = await axios.get(`/api/tickets/${id}/comments`, { headers: { Authorization: `Bearer ${token}` } });
    setComments(res.data);
  };

  const fetchAuditLogs = async () => {
    const token = localStorage.getItem("jwt_token");
    const res = await axios.get(`/api/tickets/${id}/audit`, { headers: { Authorization: `Bearer ${token}` } });
    setAuditLogs(res.data);
  };

  const handleSendComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    try {
      const token = localStorage.getItem("jwt_token");
      await axios.post(
        `/api/tickets/${id}/comments`, 
        { text: newComment },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setNewComment("");
    } catch (err) {
      alert("Failed to send message");
    }
  };

  const handleAiDraft = async () => {
    if (aiDisabled) return;
    setIsDrafting(true); 

    try {
      const token = localStorage.getItem("jwt_token");
      const res = await axios.get(`/api/ai/tickets/${id}/smart-reply`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setNewComment(res.data.reply);
      setIsDrafting(false);
      
    } catch (err) {
      setIsDrafting(false);
      
      if (err.response?.status === 429) {
        setAiDisabled(true);
        alert("AI Daily Quota Exceeded. Please type manually until tomorrow.");
      } else {
        alert("AI Service is currently unavailable. Please try again later.");
      }
    }
  };

  const downloadAttachment = async (attachmentId, fileName) => {
    try {
      const token = localStorage.getItem("jwt_token");
      const response = await axios.get(`/api/tickets/${id}/attachments/${attachmentId}`, {
        responseType: 'blob',
        headers: { Authorization: `Bearer ${token}` }
      });
      
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert("Failed to download file");
    }
  };

  if (!ticket || !currentUser) return <div className="p-8 text-center">Loading...</div>;

  const isAgent = currentUser.role === 'ROLE_AGENT';

  return (
    <div className="min-h-screen bg-gray-50 pb-12">
      <nav className="bg-white shadow-sm border-b border-gray-200 px-6 py-4 flex justify-between items-center sticky top-0 z-10">
        <div className="flex items-center gap-4">
          <button onClick={() => navigate("/dashboard")} className="text-gray-500 hover:text-gray-800 font-medium">
            &larr; Back
          </button>
          <h2 className="text-xl font-bold text-gray-800">Ticket #{ticket.id}</h2>
        </div>
      </nav>

      <main className="max-w-6xl mx-auto mt-8 px-6 grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Left Column: Details & Chat */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
            <div className="flex justify-between items-start mb-4">
              <h1 className="text-2xl font-bold text-gray-900">{ticket.title}</h1>
              <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider border ${ticket.priority === 'CRITICAL' ? 'bg-red-100 text-red-800 border-red-200' : 'bg-gray-100 text-gray-800 border-gray-200'}`}>
                {ticket.status}
              </span>
            </div>
            <p className="text-gray-700 whitespace-pre-wrap mb-6">{ticket.description}</p>
            
            {attachments.length > 0 && (
              <div className="mb-6">
                <h4 className="text-sm font-semibold text-gray-700 mb-2">Attachments</h4>
                <div className="flex flex-wrap gap-2">
                  {attachments.map(att => (
                    <button
                      key={att.id}
                      onClick={() => downloadAttachment(att.id, att.fileName)}
                      className="flex items-center gap-2 px-3 py-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm rounded border border-gray-300 transition-colors"
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path></svg>
                      {att.fileName}
                    </button>
                  ))}
                </div>
              </div>
            )}

            <div className="flex gap-6 text-sm text-gray-500 border-t border-gray-100 pt-4">
              <p><strong>Reporter:</strong> {ticket.createdBy?.name}</p>
              <p><strong>Assignee:</strong> {ticket.assignedTo ? ticket.assignedTo.name : 'Unassigned'}</p>
            </div>
          </div>

          <div className="bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col h-[500px]">
            <div className="p-4 border-b border-gray-100 bg-gray-50 rounded-t-xl">
              <h3 className="font-semibold text-gray-700">Communication Thread</h3>
            </div>
            
            <div className="flex-1 p-6 overflow-y-auto space-y-4">
              {comments.length === 0 ? (
                <p className="text-center text-gray-400 mt-10">No messages yet.</p>
              ) : (
                comments.map(comment => {
                  const isMe = comment.author.id === currentUser.id;
                  return (
                    <div key={comment.id} className={`flex ${isMe ? 'justify-end' : 'justify-start'}`}>
                      <div className={`max-w-[75%] rounded-lg px-4 py-3 ${isMe ? 'bg-blue-600 text-white rounded-br-none' : 'bg-gray-100 text-gray-800 rounded-bl-none'}`}>
                        {!isMe && <p className="text-xs font-bold mb-1 text-gray-500">{comment.author.name}</p>}
                        <p className="text-sm whitespace-pre-wrap">{comment.text}</p>
                        <p className={`text-[10px] mt-2 text-right ${isMe ? 'text-blue-200' : 'text-gray-400'}`}>
                          {new Date(comment.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </p>
                      </div>
                    </div>
                  );
                })
              )}
              <div ref={messagesEndRef} />
            </div>

            <form onSubmit={handleSendComment} className="p-4 border-t border-gray-100 bg-white rounded-b-xl">
              <div className="flex gap-3 mb-3">
                <textarea
                  rows="2"
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  placeholder="Type your message..."
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                  disabled={ticket.status === 'RESOLVED' || isDrafting}
                />
              </div>
              <div className="flex justify-between items-center">
                {isAgent && ticket.status !== 'RESOLVED' && !aiDisabled ? (
                  <button
                    type="button"
                    onClick={handleAiDraft}
                    disabled={isDrafting}
                    className="flex items-center gap-2 px-4 py-2 text-sm font-semibold text-purple-700 bg-purple-50 hover:bg-purple-100 border border-purple-200 rounded-lg transition-colors"
                  >
                    {isDrafting ? <span className="animate-pulse">✨ Analyzing Context...</span> : <span>✨ AI Smart Draft</span>}
                  </button>
                ) : <div></div>}
                
                <button 
                  type="submit" 
                  disabled={ticket.status === 'RESOLVED' || !newComment.trim() || isDrafting}
                  className="px-8 py-2 bg-blue-600 text-white font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  Send
                </button>
              </div>
            </form>
          </div>
        </div>

        {/* Right Column: Audit Timeline */}
        <div className="lg:col-span-1">
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 flex flex-col h-fit">
            <div className="p-4 border-b border-gray-100 bg-gray-50 rounded-t-xl">
              <h3 className="font-semibold text-gray-700">Ticket Timeline</h3>
            </div>
            <div className="p-6">
              {auditLogs.length === 0 ? (
                <p className="text-sm text-gray-400 text-center">No history recorded.</p>
              ) : (
                <div className="relative border-l-2 border-gray-100 ml-3 space-y-6">
                  {auditLogs.map(log => (
                    <div key={log.id} className="pl-6 relative">
                      <div className="absolute w-3 h-3 bg-blue-500 rounded-full -left-[7px] top-1.5 ring-4 ring-white"></div>
                      <p className="text-sm font-bold text-gray-800">{log.action}</p>
                      <p className="text-xs text-gray-600 mt-1">{log.details}</p>
                      <p className="text-[10px] text-gray-400 mt-1">
                        {new Date(log.createdAt).toLocaleString()} • {log.changedBy}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

      </main>
    </div>
  );
}