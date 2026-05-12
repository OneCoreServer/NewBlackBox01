// ============================================================================
// USAGE EXAMPLE - BGMI Google Login with Virtual Blackbox SDK
// ============================================================================

#include "SDK/Auth/GoogleLoginProxy.hpp"
#include "SDK/Core/Memory.hpp"
#include <iostream>
#include <string>
#include <functional>

using namespace SDK::Auth;

void Example_AsyncLogin() {
    auto &auth = GoogleLoginProxy::Get();
    std::string device_id = "your_device_id_here";

    bool started = auth.Login(
        device_id,
        [](const GoogleAuthToken &token) {
            std::cout << "Login Successful!\n";
            std::cout << "Access Token: " << token.access_token.substr(0, 20) << "...\n";
            std::cout << "Expires In: " << token.expires_in << " seconds\n";
        },
        [](const std::string &error) { std::cout << "Login Failed: " << error << "\n"; },
        [](ELoginStatus, const std::string &message) { std::cout << "Status: " << message << "\n"; });

    if (!started) {
        std::cout << "Failed to start login flow\n";
    }
}

void Example_BlockingLogin() {
    auto &auth = GoogleLoginProxy::Get();

    GoogleAuthToken token;
    bool success = auth.LoginBlocking("device_id", 120000, token);

    if (success) {
        std::cout << "Token: " << token.access_token << "\n";
    } else {
        std::cout << "Login failed or timeout\n";
    }
}

class BGMIAuthManager {
public:
    void Initialize() {
        if (LoadSavedToken()) {
            if (m_token.IsValid()) {
                InjectTokenIntoGame();
                return;
            }

            if (RefreshToken()) {
                InjectTokenIntoGame();
                return;
            }
        }

        StartGoogleLogin();
    }

private:
    GoogleAuthToken m_token;

    bool LoadSavedToken() { return false; }
    void SaveToken() {}

    bool RefreshToken() {
        auto &auth = GoogleLoginProxy::Get();
        return auth.RefreshToken(m_token);
    }

    void StartGoogleLogin() {
        auto &auth = GoogleLoginProxy::Get();

        auth.Login(GetDeviceId(),
                   [this](const GoogleAuthToken &token) {
                       m_token = token;
                       SaveToken();
                       InjectTokenIntoGame();
                   },
                   [this](const std::string &error) { ShowError("Google Login Failed: " + error); },
                   [](ELoginStatus, const std::string &msg) { UpdateUIStatus(msg); });
    }

    void InjectTokenIntoGame() {
        // Integration placeholder for game-side token usage.
    }

    std::string GetDeviceId() {
        return "device_" + std::to_string(std::hash<std::string>{}("unique_id"));
    }

    void ShowError(const std::string &) {}
    static void UpdateUIStatus(const std::string &) {}
};

static std::string GetVirtualDeviceId() {
    return "virtual_device";
}

static void InjectTokenVirtual(const GoogleAuthToken &) {}
static void HandleVirtualLoginError(const std::string &) {}
static void DirectGoogleLogin() {}

void Example_VirtualModeLogin() {
    bool is_virtual = SDK::Memory::IsVirtualMode();

    if (is_virtual) {
        auto &auth = GoogleLoginProxy::Get();

        auth.Login(GetVirtualDeviceId(),
                   [](const GoogleAuthToken &token) { InjectTokenVirtual(token); },
                   [](const std::string &error) { HandleVirtualLoginError(error); });
    } else {
        DirectGoogleLogin();
    }
}
