#include <string>
#include <windows.h>
#include <iostream>

#include "ButtonControl.h"
#include "ButtonProvider.h"

bool ButtonControl::Initialized = false;

/**** Button methods ***/

// ButtonControl: Constructor. Sets the default values for the button.
ButtonControl::ButtonControl() : m_buttonProvider(NULL), m_buttonName(L"Button"), m_buttonControlHWND(0), m_focused(false)
{
	// Nothing to do here.
}

// ~ButtonControl: Release the reference to the ButtonProvider if there is one.
ButtonControl::~ButtonControl()
{
	if (m_buttonProvider != NULL)
	{
		m_buttonProvider->Release();
		m_buttonProvider = NULL;
	}
}

// GetButtonProvider: Gets the UI Automation provider for this control or creates one.
ButtonProvider* ButtonControl::GetButtonProvider(_In_ HWND hwnd)
{
	if (m_buttonProvider == NULL)
	{
		m_buttonProvider = new ButtonProvider(hwnd, this);
		UiaRaiseAutomationEvent(m_buttonProvider, UIA_Window_WindowOpenedEventId);
	}
	return m_buttonProvider;
}

// GetHWND: Get the HWND associated with this control.
HWND ButtonControl::GetHWND()
{
	return m_buttonControlHWND;
}

// InvokeButton: Handle button click or invoke.
void ButtonControl::InvokeButton(_In_ HWND hwnd)
{

	if (UiaClientsAreListening())
	{
		// Raise an event.
		UiaRaiseAutomationEvent(GetButtonProvider(hwnd), UIA_Invoke_InvokedEventId);
	}

}

// RegisterButtonControl: Registers the ButtonControl with Windows API so that it can used
bool ButtonControl::Initialize(_In_ HINSTANCE hInstance)
{
	WNDCLASSEXW wc;

	ZeroMemory(&wc, sizeof(wc));
	wc.cbSize = sizeof(wc);
	wc.style = CS_HREDRAW | CS_VREDRAW;
	wc.lpfnWndProc = StaticButtonControlWndProc;
	wc.hInstance = hInstance;
	wc.hCursor = LoadCursor(NULL, IDC_ARROW);
	wc.lpszClassName = L"QUORUM_BUTTON";

	if (RegisterClassExW(&wc) == 0)
	{
		// An error occured. Output this error so it can be seen from Quorum.
		DWORD errorMessageID = ::GetLastError();

		LPSTR messageBuffer = nullptr;
		size_t size = FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
			NULL, errorMessageID, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPSTR)&messageBuffer, 0, NULL);

		std::string message(messageBuffer, size);
		std::cout << "RegisterButtonControl Error " << errorMessageID << ": " << message << std::endl;
		fflush(stdout);

		//Free the buffer.
		LocalFree(messageBuffer);

		return false;
	}

	return true;
}

HWND ButtonControl::Create(_In_ HINSTANCE instance, _In_ WCHAR* buttonName, _In_ WCHAR* buttonDescription)
{
	UNREFERENCED_PARAMETER(buttonDescription);
	if (!Initialized)
	{
		Initialized = Initialize(instance);
	}

	if (Initialized)
	{
		ButtonControl * control = new ButtonControl();

		CreateWindowExW(WS_EX_WINDOWEDGE,
			L"QUORUM_BUTTON",
			buttonName,
			WS_VISIBLE | WS_CHILD,
			-1,
			-1,
			1,
			1,
			GetMainWindowHandle(), // Parent window
			NULL,
			instance,
			static_cast<PVOID>(control));

		if (control->m_buttonControlHWND == 0)
		{
			DWORD errorMessageID = ::GetLastError();

			LPSTR messageBuffer = nullptr;
			size_t size = FormatMessageA(FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
				NULL, errorMessageID, MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), (LPSTR)&messageBuffer, 0, NULL);

			std::string message(messageBuffer, size);
			std::cout << "Native Code - CreateWindowExW Error " << errorMessageID << ": " << message;
			fflush(stdout);

			//Free the buffer.
			LocalFree(messageBuffer);
		}
		else
		{
			control->SetName(buttonName);
			return control->m_buttonControlHWND;
		}
	}

	return 0; // Indicates failure to create window.

}

WCHAR* ButtonControl::GetName()
{
	return m_buttonName;
}

void ButtonControl::SetName(_In_ WCHAR* name)
{
	m_buttonName = name;
}

void ButtonControl::SetControlFocus()
{
	m_focused = true;
	m_buttonProvider->NotifyFocusGained();
}

void ButtonControl::KillControlFocus()
{
	m_focused = false;
}

bool ButtonControl::HasFocus()
{
	return m_focused;
}

LRESULT CALLBACK ButtonControl::StaticButtonControlWndProc(_In_ HWND hwnd, _In_ UINT message, _In_ WPARAM wParam, _In_ LPARAM lParam)
{
	ButtonControl * pThis = reinterpret_cast<ButtonControl*>(GetWindowLongPtr(hwnd, GWLP_USERDATA));
	if (message == WM_NCCREATE)
	{
		CREATESTRUCT *createStruct = reinterpret_cast<CREATESTRUCT*>(lParam);
		pThis = reinterpret_cast<ButtonControl*>(createStruct->lpCreateParams);
		SetWindowLongPtr(hwnd, GWLP_USERDATA, reinterpret_cast<LONG_PTR>(pThis));
		pThis->m_buttonControlHWND = hwnd;
	}

	if (message == WM_NCDESTROY)
	{
		pThis = NULL;
		SetWindowLongPtr(hwnd, GWLP_USERDATA, NULL);
	}

	if (pThis != NULL)
	{
		return pThis->ButtonControlWndProc(hwnd, message, wParam, lParam);
	}

	return DefWindowProc(hwnd, message, wParam, lParam);
}

// Control window procedure.
LRESULT CALLBACK ButtonControl::ButtonControlWndProc(_In_ HWND hwnd, _In_ UINT message, _In_ WPARAM wParam, _In_ LPARAM lParam)
{
	LRESULT lResult = 0;

	switch (message)
	{
	case WM_GETOBJECT:
	{
		if (static_cast<long>(lParam) == static_cast<long>(UiaRootObjectId))
		{
			// Register with UI Automation.
			return UiaReturnRawElementProvider(hwnd, wParam, lParam, this->GetButtonProvider(this->m_buttonControlHWND));
		}

		break;
	}
	case WM_DESTROY:
	{
		lResult = UiaReturnRawElementProvider(hwnd, 0, 0, NULL);
	}
	case WM_SETFOCUS:
	{
		this->SetControlFocus();
		break;
	}
	case WM_KILLFOCUS:
	{
		this->KillControlFocus();
		break;
	}
	case QUORUM_INVOKEBUTTON:
	{
		this->InvokeButton(hwnd);
		break;
	}
	case QUORUM_SETNAME:
	{
		this->SetName((WCHAR*)lParam);
		break;
	}
	default:
		lResult = ForwardMessage(hwnd, message, wParam, lParam);
		break;
	}  // switch (message)

	return lResult;
}