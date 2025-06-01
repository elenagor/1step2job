
using System.Threading.Tasks;
using OstjApi.Models;

namespace OstjApi.Services
{
    public interface IPersonService
    {
        ValueTask<Person?> GetPersonAsync(int id);
        Task<Person> SaveProfileFromResumeAsync(int personId, string fileName, string contentType, byte[] content);
    }
}