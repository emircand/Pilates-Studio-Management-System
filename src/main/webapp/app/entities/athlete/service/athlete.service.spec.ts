import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IAthlete } from '../athlete.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../athlete.test-samples';

import { AthleteService, RestAthlete } from './athlete.service';

const requireRestSample: RestAthlete = {
  ...sampleWithRequiredData,
  birthday: sampleWithRequiredData.birthday?.toJSON(),
};

describe('Athlete Service', () => {
  let service: AthleteService;
  let httpMock: HttpTestingController;
  let expectedResult: IAthlete | IAthlete[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(AthleteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a Athlete', () => {
      const athlete = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(athlete).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Athlete', () => {
      const athlete = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(athlete).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Athlete', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Athlete', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Athlete', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Athlete', () => {
      const queryObject: any = {
        page: 0,
        size: 20,
        query: '',
        sort: [],
      };
      service.search(queryObject).subscribe(() => expectedResult);

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
      expect(expectedResult).toBe(null);
    });

    describe('addAthleteToCollectionIfMissing', () => {
      it('should add a Athlete to an empty array', () => {
        const athlete: IAthlete = sampleWithRequiredData;
        expectedResult = service.addAthleteToCollectionIfMissing([], athlete);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(athlete);
      });

      it('should not add a Athlete to an array that contains it', () => {
        const athlete: IAthlete = sampleWithRequiredData;
        const athleteCollection: IAthlete[] = [
          {
            ...athlete,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addAthleteToCollectionIfMissing(athleteCollection, athlete);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Athlete to an array that doesn't contain it", () => {
        const athlete: IAthlete = sampleWithRequiredData;
        const athleteCollection: IAthlete[] = [sampleWithPartialData];
        expectedResult = service.addAthleteToCollectionIfMissing(athleteCollection, athlete);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(athlete);
      });

      it('should add only unique Athlete to an array', () => {
        const athleteArray: IAthlete[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const athleteCollection: IAthlete[] = [sampleWithRequiredData];
        expectedResult = service.addAthleteToCollectionIfMissing(athleteCollection, ...athleteArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const athlete: IAthlete = sampleWithRequiredData;
        const athlete2: IAthlete = sampleWithPartialData;
        expectedResult = service.addAthleteToCollectionIfMissing([], athlete, athlete2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(athlete);
        expect(expectedResult).toContain(athlete2);
      });

      it('should accept null and undefined values', () => {
        const athlete: IAthlete = sampleWithRequiredData;
        expectedResult = service.addAthleteToCollectionIfMissing([], null, athlete, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(athlete);
      });

      it('should return initial array if no Athlete is added', () => {
        const athleteCollection: IAthlete[] = [sampleWithRequiredData];
        expectedResult = service.addAthleteToCollectionIfMissing(athleteCollection, undefined, null);
        expect(expectedResult).toEqual(athleteCollection);
      });
    });

    describe('compareAthlete', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareAthlete(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareAthlete(entity1, entity2);
        const compareResult2 = service.compareAthlete(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareAthlete(entity1, entity2);
        const compareResult2 = service.compareAthlete(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareAthlete(entity1, entity2);
        const compareResult2 = service.compareAthlete(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
