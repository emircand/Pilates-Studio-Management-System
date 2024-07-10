import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { IStaff } from '../staff.model';
import { sampleWithRequiredData, sampleWithNewData, sampleWithPartialData, sampleWithFullData } from '../staff.test-samples';

import { StaffService, RestStaff } from './staff.service';

const requireRestSample: RestStaff = {
  ...sampleWithRequiredData,
  birthday: sampleWithRequiredData.birthday?.toJSON(),
  hireDate: sampleWithRequiredData.hireDate?.toJSON(),
  role: sampleWithRequiredData.role?.toJSON(),
};

describe('Staff Service', () => {
  let service: StaffService;
  let httpMock: HttpTestingController;
  let expectedResult: IStaff | IStaff[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(StaffService);
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

    it('should create a Staff', () => {
      const staff = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(staff).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Staff', () => {
      const staff = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(staff).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Staff', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Staff', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Staff', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    it('should handle exceptions for searching a Staff', () => {
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

    describe('addStaffToCollectionIfMissing', () => {
      it('should add a Staff to an empty array', () => {
        const staff: IStaff = sampleWithRequiredData;
        expectedResult = service.addStaffToCollectionIfMissing([], staff);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(staff);
      });

      it('should not add a Staff to an array that contains it', () => {
        const staff: IStaff = sampleWithRequiredData;
        const staffCollection: IStaff[] = [
          {
            ...staff,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addStaffToCollectionIfMissing(staffCollection, staff);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Staff to an array that doesn't contain it", () => {
        const staff: IStaff = sampleWithRequiredData;
        const staffCollection: IStaff[] = [sampleWithPartialData];
        expectedResult = service.addStaffToCollectionIfMissing(staffCollection, staff);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(staff);
      });

      it('should add only unique Staff to an array', () => {
        const staffArray: IStaff[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const staffCollection: IStaff[] = [sampleWithRequiredData];
        expectedResult = service.addStaffToCollectionIfMissing(staffCollection, ...staffArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const staff: IStaff = sampleWithRequiredData;
        const staff2: IStaff = sampleWithPartialData;
        expectedResult = service.addStaffToCollectionIfMissing([], staff, staff2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(staff);
        expect(expectedResult).toContain(staff2);
      });

      it('should accept null and undefined values', () => {
        const staff: IStaff = sampleWithRequiredData;
        expectedResult = service.addStaffToCollectionIfMissing([], null, staff, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(staff);
      });

      it('should return initial array if no Staff is added', () => {
        const staffCollection: IStaff[] = [sampleWithRequiredData];
        expectedResult = service.addStaffToCollectionIfMissing(staffCollection, undefined, null);
        expect(expectedResult).toEqual(staffCollection);
      });
    });

    describe('compareStaff', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareStaff(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 123 };
        const entity2 = null;

        const compareResult1 = service.compareStaff(entity1, entity2);
        const compareResult2 = service.compareStaff(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 456 };

        const compareResult1 = service.compareStaff(entity1, entity2);
        const compareResult2 = service.compareStaff(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 123 };
        const entity2 = { id: 123 };

        const compareResult1 = service.compareStaff(entity1, entity2);
        const compareResult2 = service.compareStaff(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
