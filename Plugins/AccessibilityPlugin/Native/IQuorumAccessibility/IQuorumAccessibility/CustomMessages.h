#include "Resources.h"

#ifndef CustomMessages_HEADER
#define CustomMessages_HEADER

/*
	Custom Messages for the library. These are used in the different window procedures for various purposes.
	They're all defined here in a header file so that way they are consistent across the library.

	Since we are always forwarding messages sent to our accessible controls onto the main GLFW window
	We need a way to differentiate when Quorum has sent a message to the native code and when Windows has
	sent us a message.

	Only certain messages should be forwarded to the main GLFW window. They are taken care of in the 
	ForwardMessage function below.
*/

// Button messages
#define QUORUM_INVOKEBUTTON WM_USER + 2

// ForwardMessage: Sends unhandled messages to either the main GLFW window or the Default Window Procedure
inline LRESULT ForwardMessage(_In_ HWND hwnd, _In_ UINT message, _In_ WPARAM wParam, _In_ LPARAM lParam)
{
	switch (message)
	{
	// These are the messages the GLFW Window handles that we should be forwarding to it.
	case WM_DEVICECHANGE:
	{
		/*
			If wParam is any of these messages:
				* DBT_DEVNODES_CHANGED     - 0x0007
				* DBT_DEVICEARRIVAL		   - 0x8000
				* DBT_DEVICEREMOVECOMPLETE - 0x8004
			forward the message to the main window.
		*/
		if (wParam == 0x0007 || wParam == 0x8000 || wParam == 0x8004)
			return SendMessage(GetMainWindowHandle(), message, wParam, lParam);
		else 
			return DefWindowProc(hwnd, message, wParam, lParam);
	}
	case WM_SYSCOMMAND:
	{
		/*
			If wParam is any of these messages:
				* SC_SCREENSAVE			   - 0xF140
				* SC_MONITORPOWER		   - 0xF170
				* SC_KEYMENU			   - 0xF100
			forward the message to the main window.
		*/
		if (wParam == 0xF140 || wParam == 0xF170 || wParam == 0xF100)
			return SendMessage(GetMainWindowHandle(), message, wParam, lParam);
		else
			return DefWindowProc(hwnd, message, wParam, lParam);
	}
	case WM_CHAR:
	case WM_SYSCHAR:
	case WM_UNICHAR:
	case WM_KEYDOWN:
	case WM_SYSKEYDOWN:
	case WM_KEYUP:
	case WM_SYSKEYUP:
		return SendMessage(GetMainWindowHandle(), message, wParam, lParam);
	default:
		return DefWindowProc(hwnd, message, wParam, lParam);
	}
}

#endif
