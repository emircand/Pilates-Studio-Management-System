import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ISessionPackage } from '../session-package.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../session-package.test-samples';

import { SessionPackageService, RestSessionPackage } from './session-package.service';

const requireRestSample: RestSessionPackage = {
  ...sampleWithRequiredData,
  startDate: sampleWithRequiredData.startDate?.toJSON(),
  endDate: sampleWithRequiredData.endDate?.toJSON(),
};

describe('SessionPackage Service', () => {
  let service: SessionPackageService;
  let httpMock: HttpTestingController;
  let expectedResult: ISessionPackage | ISessionPackage[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(SessionPackageService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find('ABC').subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a SessionPackage', () => {
      const sessionPackage = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(sessionPackage).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a SessionPackage', () => {
      const sessionPackage = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(sessionPackage).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a SessionPackage', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of SessionPackage', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a SessionPackage', () => {
      const expected = true;

      service.delete('ABC').subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a SessionPackage', () => {
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

    describe('addSessionPackageToCollectionIfMissing', () => {
      it('should add a SessionPackage to an empty array', () => {
        const sessionPackage: ISessionPackage = sampleWithRequiredData;
        expectedResult = service.addSessionPackageToCollectionIfMissing([], sessionPackage);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(sessionPackage);
      });

      it('should not add a SessionPackage to an array that contains it', () => {
        const sessionPackage: ISessionPackage = sampleWithRequiredData;
        const sessionPackageCollection: ISessionPackage[] = [
          {
            ...sessionPackage,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addSessionPackageToCollectionIfMissing(sessionPackageCollection, sessionPackage);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a SessionPackage to an array that doesn't contain it", () => {
        const sessionPackage: ISessionPackage = sampleWithRequiredData;
        const sessionPackageCollection: ISessionPackage[] = [sampleWithPartialData];
        expectedResult = service.addSessionPackageToCollectionIfMissing(sessionPackageCollection, sessionPackage);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(sessionPackage);
      });

      it('should add only unique SessionPackage to an array', () => {
        const sessionPackageArray: ISessionPackage[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const sessionPackageCollection: ISessionPackage[] = [sampleWithRequiredData];
        expectedResult = service.addSessionPackageToCollectionIfMissing(sessionPackageCollection, ...sessionPackageArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const sessionPackage: ISessionPackage = sampleWithRequiredData;
        const sessionPackage2: ISessionPackage = sampleWithPartialData;
        expectedResult = service.addSessionPackageToCollectionIfMissing([], sessionPackage, sessionPackage2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(sessionPackage);
        expect(expectedResult).toContain(sessionPackage2);
      });

      it('should accept null and undefined values', () => {
        const sessionPackage: ISessionPackage = sampleWithRequiredData;
        expectedResult = service.addSessionPackageToCollectionIfMissing([], null, sessionPackage, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(sessionPackage);
      });

      it('should return initial array if no SessionPackage is added', () => {
        const sessionPackageCollection: ISessionPackage[] = [sampleWithRequiredData];
        expectedResult = service.addSessionPackageToCollectionIfMissing(sessionPackageCollection, undefined, null);
        expect(expectedResult).toEqual(sessionPackageCollection);
      });
    });

    describe('compareSessionPackage', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareSessionPackage(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 'ABC' };
        const entity2 = null;

        const compareResult1 = service.compareSessionPackage(entity1, entity2);
        const compareResult2 = service.compareSessionPackage(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 'ABC' };
        const entity2 = { id: 'CBA' };

        const compareResult1 = service.compareSessionPackage(entity1, entity2);
        const compareResult2 = service.compareSessionPackage(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 'ABC' };
        const entity2 = { id: 'ABC' };

        const compareResult1 = service.compareSessionPackage(entity1, entity2);
        const compareResult2 = service.compareSessionPackage(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
