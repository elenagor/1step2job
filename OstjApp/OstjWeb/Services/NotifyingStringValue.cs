using System.ComponentModel;
using System.Runtime.CompilerServices;

namespace OstjWeb.Services
{
    public class NotifyingStringValue : INotifyPropertyChanged
    {
        public event PropertyChangedEventHandler? PropertyChanged;
        private string? stringValue;

        public string? Value
        {
            get => stringValue;
            set
            {
                if (stringValue != value)
                {
                    stringValue = value;
                    OnPropertyChanged();
                }
            }
        }

        protected virtual void OnPropertyChanged(
            [CallerMemberName] string? propertyName = default)
                => PropertyChanged?.Invoke(this, new(propertyName));
    }
}