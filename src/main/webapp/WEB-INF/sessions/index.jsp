<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="zh" class="h-full">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>聊天联系人</title>
    <link href="/assets/css/dist/styles.css" rel="stylesheet">
</head>
<body class="h-full bg-gray-100">
    <div class="flex flex-col h-full">
        <!-- 置顶栏 -->
        <header class="bg-white shadow-md p-4 flex items-center justify-between">
            <div class="flex items-center">
                <h1 class="text-xl font-bold text-gray-800">联系人</h1>
            </div>
            <div class="flex items-center space-x-4">
                <button class="text-gray-600 hover:text-gray-800">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
                    </svg>
                </button>
                <button class="text-gray-600 hover:text-gray-800">
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                </button>
            </div>
        </header>

        <!-- 联系人列表 -->
        <main class="flex-grow overflow-y-auto p-4 space-y-4">
            <% 
                // 模拟联系人数据
                String[][] contacts = {
                    {"Alice", "最近怎么样？", "2小时前", "https://randomuser.me/api/portraits/women/1.jpg"},
                    {"Bob", "项目进展如何", "昨天", "https://randomuser.me/api/portraits/men/1.jpg"},
                    {"Charlie", "周末一起吃饭吗", "3天前", "https://randomuser.me/api/portraits/men/2.jpg"},
                    {"David", "新年快乐！", "1周前", "https://randomuser.me/api/portraits/men/3.jpg"},
                    {"Eve", "工作顺利吗", "2周前", "https://randomuser.me/api/portraits/women/2.jpg"}
                };
            %>

            <% for(String[] contact : contacts) { %>
                <div class="bg-white rounded-lg shadow-md p-4 flex items-center hover:bg-gray-50 transition duration-200 cursor-pointer">
                    <img src="<%= contact[3] %>" alt="<%= contact[0] %>" class="w-12 h-12 rounded-full mr-4 object-cover">
                    <div class="flex-grow">
                        <div class="flex justify-between items-center">
                            <h2 class="text-lg font-semibold text-gray-800"><%= contact[0] %></h2>
                            <span class="text-sm text-gray-500"><%= contact[2] %></span>
                        </div>
                        <p class="text-gray-600 text-sm truncate"><%= contact[1] %></p>
                    </div>
                </div>
            <% } %>
        </main>

        <!-- 置底栏 -->
        <footer class="bg-white shadow-md p-4 flex justify-around items-center">
            <button class="text-gray-600 hover:text-blue-600 flex flex-col items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
                </svg>
                <span class="text-xs mt-1">首页</span>
            </button>
            <button class="text-blue-600 flex flex-col items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <span class="text-xs mt-1">联系人</span>
            </button>
            <button class="text-gray-600 hover:text-blue-600 flex flex-col items-center">
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z" />
                </svg>
                <span class="text-xs mt-1">消息</span>
            </button>
        </footer>
    </div>
</body>
</html>
