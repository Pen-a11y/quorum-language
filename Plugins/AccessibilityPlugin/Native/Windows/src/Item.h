#pragma once

#include <windows.h>
#include <UIAutomation.h>
#include <string>
#include <atomic>
#include <wil/com.h>

#include "jni.h"
#include "Resources.h"

class Item
{
public:
	Item(JNIEnv* env, std::wstring&& controlName, std::wstring&& controlDescription, jobject jItem);
	virtual ~Item();

	virtual void Focus(bool isFocused);
	virtual bool HasFocus();

	HWND GetHWND();
	void SetName(_In_ std::wstring name);
	const WCHAR* GetName();
	void SetDescription(_In_ std::wstring description);
	const WCHAR* GetDescription();
	jobject GetMe();
	int GetHashCode();
	void SetHashCode(int hash);
	int GetUniqueId() const noexcept;

	// TODO: Change the return type to Item* once we're rid of the HWNDs.
	jlong SetFocus();

	Item* GetParent() const noexcept;
	Item* GetFirstChild() const noexcept;
	Item* GetLastChild() const noexcept;
	Item* GetPreviousSibling() const noexcept;
	Item* GetNextSibling() const noexcept;
	int GetChildCount() const noexcept;
	Item* GetRoot() const noexcept;
	void AppendChild(_In_ Item* child);
	void RemoveFromParent();

	// TODO: Make this a pure virtual function after the big refactoring.
	virtual wil::com_ptr<IRawElementProviderFragment> GetProviderFragment();

	// TODO: Drop this temporary function once we've gone fully windowless.
	virtual bool CanContainWindowlessControls() const noexcept;

protected:
	std::wstring m_ControlName;
	std::wstring m_ControlDescription;
	HWND m_ControlHWND = nullptr;
	bool focused = false;
	int objectHash;
	jobject javaItem;

private:
	void SetRootRecursive(_In_ Item* root) noexcept;
	void NotifyChildAdded();
	void RemoveFromParentInternal() noexcept;
	void RemoveAllChildren() noexcept;

	static std::atomic<int> s_nextUniqueId;

	int m_uniqueId;

	Item* m_parent = nullptr;
	Item* m_firstChild = nullptr;
	Item* m_lastChild = nullptr;
	Item* m_previousSibling = nullptr;
	Item* m_nextSibling = nullptr;
	int m_childCount = 0;
	Item* m_root;
};
