using OstjApi.Models;

namespace OstjApi.Services
{
    public interface IPersonService
    {
        ValueTask<Person?> GetPersonAsync(int id);
        ValueTask<ProfileDetails?> GetProfileDetailsAsync(int personId, int profileId);
        Task SaveProfileAsync(int personId, ProfileDetails profile);
        Task<int> SaveProfileFromResumeAsync(int personId, string fileName, string contentType, byte[] content);
    }
}