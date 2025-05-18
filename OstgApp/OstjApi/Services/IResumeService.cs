
using System.Threading.Tasks;
using OstjApi.Models;

namespace OstjApi.Services
{
    public interface IPersonService
    {
        ValueTask<Person?> GetPersonAsync(int id);
        Task<Person> SavePersonAsync(string fileName, string contentType, byte[] content);
    }
}